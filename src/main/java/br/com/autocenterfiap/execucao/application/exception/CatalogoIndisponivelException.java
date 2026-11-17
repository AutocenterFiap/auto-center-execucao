package br.com.autocenterfiap.execucao.application.exception;

/** O Cadastro nao respondeu (fora do ar, timeout ou erro): o diagnostico nao e concluido. */
public class CatalogoIndisponivelException extends RuntimeException {

	public CatalogoIndisponivelException(String mensagem, Throwable causa) {
		super(mensagem, causa);
	}
}
