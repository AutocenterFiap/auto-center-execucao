package br.com.autocenterfiap.execucao.application.usecase;

import br.com.autocenterfiap.execucao.application.exception.ExecucaoNaoEncontradaException;
import br.com.autocenterfiap.execucao.application.port.in.IniciarReparoUseCase;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoEventPublisherPort;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoRepositoryPort;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;

/**
 * Ao receber {@code execucao.iniciar-reparo}: inicia o reparo (mantendo o mecanico do
 * diagnostico) e publica {@code execucao.iniciada}.
 */
public class IniciarReparoService implements IniciarReparoUseCase {

	private final ExecucaoRepositoryPort repository;
	private final ExecucaoEventPublisherPort publisher;

	public IniciarReparoService(ExecucaoRepositoryPort repository, ExecucaoEventPublisherPort publisher) {
		this.repository = repository;
		this.publisher = publisher;
	}

	@Override
	public Execucao iniciar(Long osId) {
		Execucao execucao = repository.buscarPorOsId(osId)
				.orElseThrow(() -> new ExecucaoNaoEncontradaException(osId));
		execucao.iniciarReparo(null);
		Execucao salva = repository.salvar(execucao);
		publisher.execucaoIniciada(salva);
		return salva;
	}
}
