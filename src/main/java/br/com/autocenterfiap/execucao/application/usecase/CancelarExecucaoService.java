package br.com.autocenterfiap.execucao.application.usecase;

import br.com.autocenterfiap.execucao.application.port.in.CancelarExecucaoUseCase;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoRepositoryPort;

/**
 * Ao receber {@code os.cancelada}: cancela a execucao (compensacao). Tolerante: se nao
 * existir execucao para a OS ou ela ja estiver terminal, nada muda.
 */
public class CancelarExecucaoService implements CancelarExecucaoUseCase {

	private final ExecucaoRepositoryPort repository;

	public CancelarExecucaoService(ExecucaoRepositoryPort repository) {
		this.repository = repository;
	}

	@Override
	public void cancelar(Long osId, String motivo) {
		repository.buscarPorOsId(osId).ifPresent(execucao -> {
			if (execucao.cancelar(motivo)) {
				repository.salvar(execucao);
			}
		});
	}
}
