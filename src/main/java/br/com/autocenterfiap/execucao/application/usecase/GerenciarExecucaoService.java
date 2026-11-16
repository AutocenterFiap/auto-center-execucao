package br.com.autocenterfiap.execucao.application.usecase;

import br.com.autocenterfiap.execucao.application.exception.ExecucaoNaoEncontradaException;
import br.com.autocenterfiap.execucao.application.port.in.GerenciarExecucaoUseCase;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoEventPublisherPort;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoRepositoryPort;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;
import br.com.autocenterfiap.execucao.domain.entity.Itens;

/**
 * Operacoes da oficina via REST: concluir diagnostico (com itens), finalizar e
 * registrar falha. Cada uma publica o evento correspondente.
 */
public class GerenciarExecucaoService implements GerenciarExecucaoUseCase {

	private final ExecucaoRepositoryPort repository;
	private final ExecucaoEventPublisherPort publisher;

	public GerenciarExecucaoService(ExecucaoRepositoryPort repository, ExecucaoEventPublisherPort publisher) {
		this.repository = repository;
		this.publisher = publisher;
	}

	@Override
	public Execucao concluirDiagnostico(Long osId, Itens itens) {
		Execucao execucao = carregar(osId);
		execucao.concluirDiagnostico(itens);
		Execucao salva = repository.salvar(execucao);
		publisher.diagnosticoConcluido(salva);
		return salva;
	}

	@Override
	public Execucao finalizar(Long osId) {
		Execucao execucao = carregar(osId);
		execucao.finalizar();
		Execucao salva = repository.salvar(execucao);
		publisher.execucaoFinalizada(salva);
		return salva;
	}

	@Override
	public Execucao registrarFalha(Long osId, String motivo) {
		Execucao execucao = carregar(osId);
		execucao.falhar(motivo);
		Execucao salva = repository.salvar(execucao);
		publisher.execucaoFalhou(salva);
		return salva;
	}

	private Execucao carregar(Long osId) {
		return repository.buscarPorOsId(osId)
				.orElseThrow(() -> new ExecucaoNaoEncontradaException(osId));
	}
}
