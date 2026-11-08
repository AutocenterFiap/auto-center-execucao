package br.com.autocenterfiap.execucao.adapter.in.consumer;

import br.com.autocenterfiap.execucao.adapter.messaging.contract.EventoEnvelope;
import br.com.autocenterfiap.execucao.adapter.messaging.contract.ExecucaoPayloads;
import br.com.autocenterfiap.execucao.adapter.messaging.contract.TipoEvento;
import br.com.autocenterfiap.execucao.application.dto.OsCriadaCommand;
import br.com.autocenterfiap.execucao.application.port.in.CancelarExecucaoUseCase;
import br.com.autocenterfiap.execucao.application.port.in.ProcessarOsCriadaUseCase;
import br.com.autocenterfiap.execucao.application.port.out.EventoProcessadoPort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

/**
 * Consome o topico {@code os.eventos} e roteia por {@code tipo}:
 * {@code os.criada} (inicia o diagnostico) e {@code os.cancelada} (compensacao).
 * Deduplica por {@code eventId} e propaga o {@code correlationId} no MDC.
 */
@Component
public class OsEventosConsumer {

	private static final Logger log = LoggerFactory.getLogger(OsEventosConsumer.class);

	private final ProcessarOsCriadaUseCase processarOsCriada;
	private final CancelarExecucaoUseCase cancelarExecucao;
	private final EventoProcessadoPort eventoProcessado;
	private final ObjectMapper objectMapper;

	public OsEventosConsumer(ProcessarOsCriadaUseCase processarOsCriada,
	                         CancelarExecucaoUseCase cancelarExecucao,
	                         EventoProcessadoPort eventoProcessado,
	                         ObjectMapper objectMapper) {
		this.processarOsCriada = processarOsCriada;
		this.cancelarExecucao = cancelarExecucao;
		this.eventoProcessado = eventoProcessado;
		this.objectMapper = objectMapper;
	}

	@RetryableTopic(
			attempts = "4",
			backoff = @Backoff(delay = 2000, multiplier = 2.0),
			dltStrategy = DltStrategy.FAIL_ON_ERROR,
			autoCreateTopics = "true")
	@KafkaListener(topics = "${execucao.kafka.topics.os-eventos}",
			groupId = "${spring.kafka.consumer.group-id}")
	public void onMensagem(String mensagem) throws Exception {
		EventoEnvelope<JsonNode> envelope = objectMapper.readValue(mensagem, new TypeReference<>() {
		});

		if (eventoProcessado.jaProcessado(envelope.eventId())) {
			log.debug("Evento {} ja processado; ignorando", envelope.eventId());
			return;
		}

		MDC.put("correlationId", envelope.correlationId());
		try {
			boolean tratado = rotear(envelope);
			if (tratado) {
				eventoProcessado.marcarProcessado(envelope.eventId());
			}
		} finally {
			MDC.remove("correlationId");
		}
	}

	private boolean rotear(EventoEnvelope<JsonNode> envelope) throws Exception {
		switch (envelope.tipo()) {
			case TipoEvento.OS_CRIADA -> {
				ExecucaoPayloads.OsCriada p =
						objectMapper.treeToValue(envelope.payload(), ExecucaoPayloads.OsCriada.class);
				processarOsCriada.processar(new OsCriadaCommand(envelope.osId(), p.numero(),
						p.clienteId(), p.veiculoId(), p.problemaRelatado(), envelope.correlationId()));
				return true;
			}
			case TipoEvento.OS_CANCELADA -> {
				ExecucaoPayloads.OsCancelada p =
						objectMapper.treeToValue(envelope.payload(), ExecucaoPayloads.OsCancelada.class);
				cancelarExecucao.cancelar(envelope.osId(), p.motivo());
				return true;
			}
			default -> {
				log.debug("Tipo {} nao tratado por os.eventos; ignorando", envelope.tipo());
				return false;
			}
		}
	}

	@DltHandler
	public void tratarDlt(String mensagem) {
		log.error("Mensagem de os.eventos enviada para a DLT: {}", mensagem);
	}
}
