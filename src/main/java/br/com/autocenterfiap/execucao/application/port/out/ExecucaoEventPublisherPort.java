package br.com.autocenterfiap.execucao.application.port.out;

import br.com.autocenterfiap.execucao.domain.entity.Execucao;

/**
 * Porta de saida para publicacao dos eventos da Execucao no topico
 * {@code execucao.eventos} (envelope padrao, chave = osId).
 */
public interface ExecucaoEventPublisherPort {

	void diagnosticoIniciado(Execucao execucao);

	void diagnosticoConcluido(Execucao execucao);

	void execucaoIniciada(Execucao execucao);

	void execucaoFinalizada(Execucao execucao);

	void execucaoFalhou(Execucao execucao);
}
