package br.com.autocenterfiap.execucao.domain.exception;

/**
 * Lancada quando os dados de criacao/reconstrucao de uma execucao sao invalidos.
 */
public class ExecucaoInvalidaException extends RuntimeException {

	public ExecucaoInvalidaException(String mensagem) {
		super(mensagem);
	}
}
