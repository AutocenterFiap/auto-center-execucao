package br.com.autocenterfiap.execucao.application.exception;

/** Item do diagnostico que nao existe ou esta inativo no catalogo do Cadastro. */
public class ItemCatalogoInvalidoException extends RuntimeException {

	public ItemCatalogoInvalidoException(String mensagem) {
		super(mensagem);
	}
}
