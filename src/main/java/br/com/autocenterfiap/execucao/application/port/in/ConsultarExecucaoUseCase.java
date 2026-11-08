package br.com.autocenterfiap.execucao.application.port.in;

import br.com.autocenterfiap.execucao.domain.entity.Execucao;

import java.util.List;

/**
 * Consultas de leitura sobre execucoes e a fila da oficina.
 */
public interface ConsultarExecucaoUseCase {

	Execucao buscarPorId(String execucaoId);

	Execucao buscarPorOsId(String osId);

	List<Execucao> listarFila();
}
