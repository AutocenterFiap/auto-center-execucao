package br.com.autocenterfiap.execucao.domain.enums;

import java.util.Set;

/**
 * Estados possiveis de uma execucao na oficina (contrato da Saga orquestrada).
 *
 * <p>Fluxo esperado:
 * {@code NA_FILA -> EM_DIAGNOSTICO -> DIAGNOSTICO_CONCLUIDO -> EM_EXECUCAO -> FINALIZADA}.
 * {@code FALHA} (reparo falhou) e {@code CANCELADA} (OS cancelada) sao terminais de
 * compensacao a partir dos estados ativos.
 */
public enum StatusExecucao {

	NA_FILA,
	EM_DIAGNOSTICO,
	DIAGNOSTICO_CONCLUIDO,
	EM_EXECUCAO,
	FINALIZADA,
	FALHA,
	CANCELADA;

	public Set<StatusExecucao> transicoesValidas() {
		return switch (this) {
			case NA_FILA -> Set.of(EM_DIAGNOSTICO, CANCELADA);
			case EM_DIAGNOSTICO -> Set.of(DIAGNOSTICO_CONCLUIDO, CANCELADA);
			case DIAGNOSTICO_CONCLUIDO -> Set.of(EM_EXECUCAO, CANCELADA);
			case EM_EXECUCAO -> Set.of(FINALIZADA, FALHA, CANCELADA);
			case FINALIZADA, FALHA, CANCELADA -> Set.of();
		};
	}

	public boolean podeTransitarPara(StatusExecucao destino) {
		return transicoesValidas().contains(destino);
	}

	public boolean ehTerminal() {
		return this == FINALIZADA || this == FALHA || this == CANCELADA;
	}
}
