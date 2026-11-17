package br.com.autocenterfiap.execucao.application.port.in;

import br.com.autocenterfiap.execucao.domain.entity.Execucao;
import br.com.autocenterfiap.execucao.domain.entity.Itens;

/**
 * Operacoes acionadas pela oficina via REST: concluir o diagnostico (com itens),
 * finalizar o reparo e registrar falha. Cada uma publica o evento correspondente.
 */
public interface GerenciarExecucaoUseCase {

	Execucao concluirDiagnostico(Long osId, Itens itens);

	Execucao finalizar(Long osId);

	Execucao registrarFalha(Long osId, String motivo);
}
