package br.com.autocenterfiap.execucao.application.exception;

/**
 * Lancada quando uma execucao nao e encontrada por id ou por osId.
 */
public class ExecucaoNaoEncontradaException extends RuntimeException {

	public ExecucaoNaoEncontradaException(String criterio, String valor) {
		super("Execucao nao encontrada para %s=%s".formatted(criterio, valor));
	}
}
