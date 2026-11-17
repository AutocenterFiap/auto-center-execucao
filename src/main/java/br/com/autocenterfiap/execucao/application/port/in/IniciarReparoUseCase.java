package br.com.autocenterfiap.execucao.application.port.in;

import br.com.autocenterfiap.execucao.domain.entity.Execucao;

/**
 * Processa {@code execucao.iniciar-reparo}: inicia o reparo e publica
 * {@code execucao.iniciada}.
 */
public interface IniciarReparoUseCase {

	Execucao iniciar(Long osId);
}
