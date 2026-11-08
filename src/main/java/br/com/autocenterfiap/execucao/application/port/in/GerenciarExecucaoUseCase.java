package br.com.autocenterfiap.execucao.application.port.in;

import br.com.autocenterfiap.execucao.domain.entity.Execucao;

/**
 * Avanca as etapas de uma execucao. Acionado pelos endpoints REST da oficina.
 */
public interface GerenciarExecucaoUseCase {

	Execucao iniciarDiagnostico(String execucaoId, String observacao);

	Execucao iniciarReparo(String execucaoId, String observacao);

	Execucao finalizar(String execucaoId, String observacao);

	Execucao registrarFalha(String execucaoId, String motivo);
}
