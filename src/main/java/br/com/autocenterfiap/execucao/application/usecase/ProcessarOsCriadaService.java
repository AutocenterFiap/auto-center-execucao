package br.com.autocenterfiap.execucao.application.usecase;

import br.com.autocenterfiap.execucao.application.dto.OsCriadaCommand;
import br.com.autocenterfiap.execucao.application.port.in.ProcessarOsCriadaUseCase;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoEventPublisherPort;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoRepositoryPort;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;

/**
 * Ao receber {@code os.criada}: enfileira a execucao, inicia o diagnostico e publica
 * {@code diagnostico.iniciado}. Idempotente por {@code osId} (nao duplica nem republica).
 */
public class ProcessarOsCriadaService implements ProcessarOsCriadaUseCase {

	private static final long POOL_MECANICOS = 5;

	private final ExecucaoRepositoryPort repository;
	private final ExecucaoEventPublisherPort publisher;

	public ProcessarOsCriadaService(ExecucaoRepositoryPort repository, ExecucaoEventPublisherPort publisher) {
		this.repository = repository;
		this.publisher = publisher;
	}

	@Override
	public Execucao processar(OsCriadaCommand comando) {
		return repository.buscarPorOsId(comando.osId())
				.orElseGet(() -> criarEIniciarDiagnostico(comando));
	}

	private Execucao criarEIniciarDiagnostico(OsCriadaCommand c) {
		long posicao = repository.proximaPosicaoFila();
		Execucao execucao = Execucao.criar(c.osId(), c.numero(), c.clienteId(), c.veiculoId(),
				c.problemaRelatado(), c.correlationId(), (int) posicao);
		execucao.iniciarDiagnostico(alocarMecanico(c.osId()));
		Execucao salva = repository.salvar(execucao);
		publisher.diagnosticoIniciado(salva);
		return salva;
	}

	private Long alocarMecanico(Long osId) {
		return Math.floorMod(osId - 1, POOL_MECANICOS) + 1;
	}
}
