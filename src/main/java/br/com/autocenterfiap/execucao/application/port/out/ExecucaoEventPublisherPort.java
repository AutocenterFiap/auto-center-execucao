package br.com.autocenterfiap.execucao.application.port.out;

import br.com.autocenterfiap.execucao.domain.entity.Execucao;

/**
 * Porta de saida para publicacao dos eventos de dominio no Kafka (implementada pela
 * camada de infraestrutura).
 */
public interface ExecucaoEventPublisherPort {

	void publicarExecucaoIniciada(Execucao execucao);

	void publicarExecucaoFinalizada(Execucao execucao);

	void publicarExecucaoFalhou(Execucao execucao);
}
