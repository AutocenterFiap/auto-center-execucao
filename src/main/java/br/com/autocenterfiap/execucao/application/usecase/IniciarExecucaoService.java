package br.com.autocenterfiap.execucao.application.usecase;

import br.com.autocenterfiap.execucao.application.dto.IniciarExecucaoCommand;
import br.com.autocenterfiap.execucao.application.port.in.IniciarExecucaoUseCase;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoEventPublisherPort;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoRepositoryPort;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;

/**
 * Coloca a OS na fila de execucao ao receber o pagamento confirmado.
 *
 * <p>Idempotente: como o consumo do Kafka e at-least-once, se ja houver uma execucao
 * para a OS, retorna a existente sem criar outra nem republicar o evento.
 */
public class IniciarExecucaoService implements IniciarExecucaoUseCase {

	private final ExecucaoRepositoryPort repository;
	private final ExecucaoEventPublisherPort publisher;

	public IniciarExecucaoService(ExecucaoRepositoryPort repository, ExecucaoEventPublisherPort publisher) {
		this.repository = repository;
		this.publisher = publisher;
	}

	@Override
	public Execucao iniciar(IniciarExecucaoCommand comando) {
		return repository.buscarPorOsId(comando.osId())
				.orElseGet(() -> criarEnfileirar(comando.osId()));
	}

	private Execucao criarEnfileirar(String osId) {
		long posicao = repository.proximaPosicaoFila();
		Execucao execucao = Execucao.criarNaFila(osId, (int) posicao);
		Execucao salva = repository.salvar(execucao);
		publisher.publicarExecucaoIniciada(salva);
		return salva;
	}
}
