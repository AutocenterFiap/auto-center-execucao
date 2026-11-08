package br.com.autocenterfiap.execucao.adapter.out.messaging;

import br.com.autocenterfiap.execucao.adapter.messaging.ItensContratoMapper;
import br.com.autocenterfiap.execucao.adapter.messaging.contract.EventoEnvelope;
import br.com.autocenterfiap.execucao.adapter.messaging.contract.ExecucaoPayloads;
import br.com.autocenterfiap.execucao.adapter.messaging.contract.TipoEvento;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoEventPublisherPort;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;
import br.com.autocenterfiap.execucao.infrastructure.config.KafkaTopicsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica os eventos da Execucao no topico {@code execucao.eventos}, usando o envelope
 * padrao e o {@code osId} como chave.
 */
@Component
public class ExecucaoKafkaPublisher implements ExecucaoEventPublisherPort {

	private static final Logger log = LoggerFactory.getLogger(ExecucaoKafkaPublisher.class);

	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final KafkaTopicsProperties topics;

	public ExecucaoKafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate, KafkaTopicsProperties topics) {
		this.kafkaTemplate = kafkaTemplate;
		this.topics = topics;
	}

	@Override
	public void diagnosticoIniciado(Execucao execucao) {
		publicar(TipoEvento.DIAGNOSTICO_INICIADO, execucao,
				new ExecucaoPayloads.DiagnosticoIniciado(execucao.getMecanicoId()));
	}

	@Override
	public void diagnosticoConcluido(Execucao execucao) {
		publicar(TipoEvento.DIAGNOSTICO_CONCLUIDO, execucao,
				new ExecucaoPayloads.DiagnosticoConcluido(ItensContratoMapper.toContrato(execucao.getItens())));
	}

	@Override
	public void execucaoIniciada(Execucao execucao) {
		publicar(TipoEvento.EXECUCAO_INICIADA, execucao,
				new ExecucaoPayloads.ExecucaoIniciada(execucao.getMecanicoId()));
	}

	@Override
	public void execucaoFinalizada(Execucao execucao) {
		publicar(TipoEvento.EXECUCAO_FINALIZADA, execucao, new ExecucaoPayloads.ExecucaoFinalizada());
	}

	@Override
	public void execucaoFalhou(Execucao execucao) {
		publicar(TipoEvento.EXECUCAO_FALHOU, execucao,
				new ExecucaoPayloads.ExecucaoFalhou(execucao.getMotivo()));
	}

	private void publicar(String tipo, Execucao execucao, Object payload) {
		EventoEnvelope<Object> envelope = EventoEnvelope.novo(
				tipo, execucao.getOsId(), execucao.getCorrelationId(), payload);
		log.info("Publicando {} para OS {} em {}", tipo, execucao.getOsId(), topics.execucaoEventos());
		kafkaTemplate.send(topics.execucaoEventos(), String.valueOf(execucao.getOsId()), envelope);
	}
}
