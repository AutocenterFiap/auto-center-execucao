package br.com.autocenterfiap.execucao.adapter.out.messaging;

import br.com.autocenterfiap.execucao.domain.entity.Execucao;
import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;

import java.time.Instant;

/**
 * Payload publicado no Kafka para os eventos de execucao. A chave da mensagem e o
 * {@code osId} (garante ordenacao por OS).
 */
public record ExecucaoEventPayload(
		String evento,
		String osId,
		String execucaoId,
		StatusExecucao status,
		String motivoFalha,
		Instant ocorridoEm) {

	public static ExecucaoEventPayload de(String evento, Execucao execucao) {
		return new ExecucaoEventPayload(
				evento,
				execucao.getOsId(),
				execucao.getId(),
				execucao.getStatus(),
				execucao.getMotivoFalha(),
				Instant.now());
	}
}
