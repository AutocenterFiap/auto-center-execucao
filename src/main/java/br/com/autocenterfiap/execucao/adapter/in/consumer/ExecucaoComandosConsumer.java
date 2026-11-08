package br.com.autocenterfiap.execucao.adapter.in.consumer;

import br.com.autocenterfiap.execucao.adapter.messaging.contract.EventoEnvelope;
import br.com.autocenterfiap.execucao.adapter.messaging.contract.TipoEvento;
import br.com.autocenterfiap.execucao.application.port.in.IniciarReparoUseCase;
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
 * Consome o topico {@code execucao.comandos} ({@code execucao.iniciar-reparo}), inicia
 * o reparo e publica {@code execucao.iniciada}. Deduplica por {@code eventId}.
 */
@Component
public class ExecucaoComandosConsumer {

	private static final Logger log = LoggerFactory.getLogger(ExecucaoComandosConsumer.class);

	private final IniciarReparoUseCase iniciarReparo;
	private final EventoProcessadoPort eventoProcessado;
	private final ObjectMapper objectMapper;

	public ExecucaoComandosConsumer(IniciarReparoUseCase iniciarReparo,
	                                EventoProcessadoPort eventoProcessado,
	                                ObjectMapper objectMapper) {
		this.iniciarReparo = iniciarReparo;
		this.eventoProcessado = eventoProcessado;
		this.objectMapper = objectMapper;
	}

	@RetryableTopic(
			attempts = "4",
			backoff = @Backoff(delay = 2000, multiplier = 2.0),
			dltStrategy = DltStrategy.FAIL_ON_ERROR,
			autoCreateTopics = "true")
	@KafkaListener(topics = "${execucao.kafka.topics.execucao-comandos}",
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
			if (TipoEvento.EXECUCAO_INICIAR_REPARO.equals(envelope.tipo())) {
				iniciarReparo.iniciar(envelope.osId());
				eventoProcessado.marcarProcessado(envelope.eventId());
			} else {
				log.debug("Tipo {} nao tratado por execucao.comandos; ignorando", envelope.tipo());
			}
		} finally {
			MDC.remove("correlationId");
		}
	}

	@DltHandler
	public void tratarDlt(String mensagem) {
		log.error("Mensagem de execucao.comandos enviada para a DLT: {}", mensagem);
	}
}
