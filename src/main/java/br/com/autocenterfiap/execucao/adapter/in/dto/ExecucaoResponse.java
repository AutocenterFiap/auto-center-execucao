package br.com.autocenterfiap.execucao.adapter.in.dto;

import br.com.autocenterfiap.execucao.domain.entity.Execucao;
import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;

import java.time.Instant;
import java.util.List;

public record ExecucaoResponse(
		String id,
		String osId,
		StatusExecucao status,
		Integer posicaoFila,
		String motivoFalha,
		Instant criadoEm,
		Instant atualizadoEm,
		Instant finalizadoEm,
		List<EventoResponse> historico) {

	public static ExecucaoResponse de(Execucao execucao) {
		List<EventoResponse> historico = execucao.getHistorico().stream()
				.map(EventoResponse::de)
				.toList();

		return new ExecucaoResponse(
				execucao.getId(),
				execucao.getOsId(),
				execucao.getStatus(),
				execucao.getPosicaoFila(),
				execucao.getMotivoFalha(),
				execucao.getCriadoEm(),
				execucao.getAtualizadoEm(),
				execucao.getFinalizadoEm(),
				historico);
	}
}
