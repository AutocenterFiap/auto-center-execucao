package br.com.autocenterfiap.execucao.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Nomes dos topicos Kafka do ecossistema, configurados em {@code execucao.kafka.topics}.
 */
@ConfigurationProperties(prefix = "execucao.kafka.topics")
public record KafkaTopicsProperties(
		String pagamentoConfirmado,
		String execucaoIniciada,
		String execucaoFinalizada,
		String execucaoFalhou) {
}
