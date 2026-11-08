package br.com.autocenterfiap.execucao.adapter.out.messaging;

import br.com.autocenterfiap.execucao.application.port.out.ExecucaoEventPublisherPort;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;
import br.com.autocenterfiap.execucao.infrastructure.config.KafkaTopicsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica os eventos de dominio da execucao no Kafka, usando o {@code osId} como chave.
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
	public void publicarExecucaoIniciada(Execucao execucao) {
		publicar(topics.execucaoIniciada(), "execucao.iniciada", execucao);
	}

	@Override
	public void publicarExecucaoFinalizada(Execucao execucao) {
		publicar(topics.execucaoFinalizada(), "execucao.finalizada", execucao);
	}

	@Override
	public void publicarExecucaoFalhou(Execucao execucao) {
		publicar(topics.execucaoFalhou(), "execucao.falhou", execucao);
	}

	private void publicar(String topico, String evento, Execucao execucao) {
		ExecucaoEventPayload payload = ExecucaoEventPayload.de(evento, execucao);
		log.info("Publicando evento {} para OS {} no topico {}", evento, execucao.getOsId(), topico);
		kafkaTemplate.send(topico, execucao.getOsId(), payload);
	}
}
