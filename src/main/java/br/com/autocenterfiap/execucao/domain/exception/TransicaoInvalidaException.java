package br.com.autocenterfiap.execucao.domain.exception;

import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;

/**
 * Lancada quando se tenta transitar uma execucao para um estado que nao e
 * permitido a partir do estado atual.
 */
public class TransicaoInvalidaException extends RuntimeException {

	public TransicaoInvalidaException(StatusExecucao atual, StatusExecucao destino) {
		super("Transicao invalida de %s para %s".formatted(atual, destino));
	}
}
