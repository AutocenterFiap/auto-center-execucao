package br.com.autocenterfiap.execucao.application.usecase;

import br.com.autocenterfiap.execucao.application.exception.ExecucaoNaoEncontradaException;
import br.com.autocenterfiap.execucao.application.port.in.ConsultarExecucaoUseCase;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoRepositoryPort;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;

import java.util.List;

/**
 * Consultas de leitura sobre execucoes e a fila da oficina.
 */
public class ConsultarExecucaoService implements ConsultarExecucaoUseCase {

	private final ExecucaoRepositoryPort repository;

	public ConsultarExecucaoService(ExecucaoRepositoryPort repository) {
		this.repository = repository;
	}

	@Override
	public Execucao buscarPorOsId(Long osId) {
		return repository.buscarPorOsId(osId)
				.orElseThrow(() -> new ExecucaoNaoEncontradaException(osId));
	}

	@Override
	public List<Execucao> listarFila() {
		return repository.listarFila();
	}
}
