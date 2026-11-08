package br.com.autocenterfiap.execucao.application.usecase;

import br.com.autocenterfiap.execucao.application.exception.ExecucaoNaoEncontradaException;
import br.com.autocenterfiap.execucao.application.port.in.GerenciarExecucaoUseCase;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoEventPublisherPort;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoRepositoryPort;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;

/**
 * Avanca as etapas de uma execucao. Ao finalizar ou falhar, publica o evento
 * correspondente para o orquestrador da Saga (OS Service).
 */
public class GerenciarExecucaoService implements GerenciarExecucaoUseCase {

	private final ExecucaoRepositoryPort repository;
	private final ExecucaoEventPublisherPort publisher;

	public GerenciarExecucaoService(ExecucaoRepositoryPort repository, ExecucaoEventPublisherPort publisher) {
		this.repository = repository;
		this.publisher = publisher;
	}

	@Override
	public Execucao iniciarDiagnostico(String execucaoId, String observacao) {
		Execucao execucao = carregar(execucaoId);
		execucao.iniciarDiagnostico(observacao);
		return repository.salvar(execucao);
	}

	@Override
	public Execucao iniciarReparo(String execucaoId, String observacao) {
		Execucao execucao = carregar(execucaoId);
		execucao.iniciarReparo(observacao);
		return repository.salvar(execucao);
	}

	@Override
	public Execucao finalizar(String execucaoId, String observacao) {
		Execucao execucao = carregar(execucaoId);
		execucao.finalizar(observacao);
		Execucao salva = repository.salvar(execucao);
		publisher.publicarExecucaoFinalizada(salva);
		return salva;
	}

	@Override
	public Execucao registrarFalha(String execucaoId, String motivo) {
		Execucao execucao = carregar(execucaoId);
		execucao.falhar(motivo);
		Execucao salva = repository.salvar(execucao);
		publisher.publicarExecucaoFalhou(salva);
		return salva;
	}

	private Execucao carregar(String execucaoId) {
		return repository.buscarPorId(execucaoId)
				.orElseThrow(() -> new ExecucaoNaoEncontradaException("id", execucaoId));
	}
}
