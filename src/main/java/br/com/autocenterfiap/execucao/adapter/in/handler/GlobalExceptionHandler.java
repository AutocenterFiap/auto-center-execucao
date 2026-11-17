package br.com.autocenterfiap.execucao.adapter.in.handler;

import br.com.autocenterfiap.execucao.application.exception.CatalogoIndisponivelException;
import br.com.autocenterfiap.execucao.application.exception.ExecucaoNaoEncontradaException;
import br.com.autocenterfiap.execucao.application.exception.ItemCatalogoInvalidoException;
import br.com.autocenterfiap.execucao.domain.exception.ExecucaoInvalidaException;
import br.com.autocenterfiap.execucao.domain.exception.TransicaoInvalidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ExecucaoNaoEncontradaException.class)
	public ResponseEntity<ErroResponse> naoEncontrada(ExecucaoNaoEncontradaException ex) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(TransicaoInvalidaException.class)
	public ResponseEntity<ErroResponse> transicaoInvalida(TransicaoInvalidaException ex) {
		return build(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(ExecucaoInvalidaException.class)
	public ResponseEntity<ErroResponse> execucaoInvalida(ExecucaoInvalidaException ex) {
		return build(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(ItemCatalogoInvalidoException.class)
	public ResponseEntity<ErroResponse> itemCatalogoInvalido(ItemCatalogoInvalidoException ex) {
		return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
	}

	@ExceptionHandler(CatalogoIndisponivelException.class)
	public ResponseEntity<ErroResponse> catalogoIndisponivel(CatalogoIndisponivelException ex) {
		return build(HttpStatus.SERVICE_UNAVAILABLE, "Cadastro indisponivel; tente novamente em instantes");
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex) {
		String mensagem = ex.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(e -> e.getField() + ": " + e.getDefaultMessage())
				.orElse("Requisicao invalida");
		return build(HttpStatus.BAD_REQUEST, mensagem);
	}

	private ResponseEntity<ErroResponse> build(HttpStatus status, String mensagem) {
		return ResponseEntity.status(status)
				.body(ErroResponse.de(status.value(), status.getReasonPhrase(), mensagem));
	}
}
