package br.com.autocenterfiap.execucao.application.exception;

/**
 * Lancada quando uma execucao nao e encontrada pelo {@code osId}.
 */
public class ExecucaoNaoEncontradaException extends RuntimeException {

	public ExecucaoNaoEncontradaException(Long osId) {
		super("Execucao nao encontrada para osId=" + osId);
	}
}
