package br.com.autocenterfiap.execucao.application.port.in;

import br.com.autocenterfiap.execucao.application.dto.IniciarExecucaoCommand;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;

/**
 * Coloca uma OS na fila de execucao (estado inicial NA_FILA). Operacao idempotente:
 * se ja existir uma execucao para a OS, retorna a existente sem duplicar.
 */
public interface IniciarExecucaoUseCase {

	Execucao iniciar(IniciarExecucaoCommand comando);
}
