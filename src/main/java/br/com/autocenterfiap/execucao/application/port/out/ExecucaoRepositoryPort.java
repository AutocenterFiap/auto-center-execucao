package br.com.autocenterfiap.execucao.application.port.out;

import br.com.autocenterfiap.execucao.domain.entity.Execucao;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saida para persistencia de execucoes (implementada pela camada de
 * infraestrutura com MongoDB).
 */
public interface ExecucaoRepositoryPort {

	Execucao salvar(Execucao execucao);

	Optional<Execucao> buscarPorId(String id);

	Optional<Execucao> buscarPorOsId(String osId);

	/**
	 * Execucoes ainda ativas (nao terminais), ordenadas pela posicao na fila.
	 */
	List<Execucao> listarFila();

	/**
	 * Proxima posicao disponivel na fila.
	 */
	long proximaPosicaoFila();
}
