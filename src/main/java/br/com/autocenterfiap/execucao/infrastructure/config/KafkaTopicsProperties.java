package br.com.autocenterfiap.execucao.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Nomes dos topicos Kafka do ecossistema, em {@code execucao.kafka.topics}.
 *
 * <ul>
 *   <li>{@code osEventos} e {@code execucaoComandos}: consumidos pela Execucao;</li>
 *   <li>{@code execucaoEventos}: publicado pela Execucao.</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "execucao.kafka.topics")
public record KafkaTopicsProperties(
		String osEventos,
		String execucaoComandos,
		String execucaoEventos) {
}
