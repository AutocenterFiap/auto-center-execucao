package br.com.autocenterfiap.execucao.domain.enums;

import java.util.Set;

/**
 * Estados possiveis de uma execucao na oficina.
 *
 * <p>Fluxo esperado:
 * {@code NA_FILA -> EM_DIAGNOSTICO -> EM_EXECUCAO -> FINALIZADA}, com {@code FALHA}
 * como estado terminal de compensacao a partir de qualquer etapa ativa.
 */
public enum StatusExecucao {

	NA_FILA,
	EM_DIAGNOSTICO,
	EM_EXECUCAO,
	FINALIZADA,
	FALHA;

	/**
	 * Retorna os estados para os quais este estado pode transitar.
	 */
	public Set<StatusExecucao> transicoesValidas() {
		return switch (this) {
			case NA_FILA -> Set.of(EM_DIAGNOSTICO, FALHA);
			case EM_DIAGNOSTICO -> Set.of(EM_EXECUCAO, FALHA);
			case EM_EXECUCAO -> Set.of(FINALIZADA, FALHA);
			case FINALIZADA, FALHA -> Set.of();
		};
	}

	public boolean podeTransitarPara(StatusExecucao destino) {
		return transicoesValidas().contains(destino);
	}

	public boolean ehTerminal() {
		return this == FINALIZADA || this == FALHA;
	}
}
