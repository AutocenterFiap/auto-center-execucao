package br.com.autocenterfiap.execucao.application.port.out;

import br.com.autocenterfiap.execucao.domain.entity.Execucao;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saida para persistencia de execucoes (MongoDB).
 */
public interface ExecucaoRepositoryPort {

	Execucao salvar(Execucao execucao);

	Optional<Execucao> buscarPorOsId(Long osId);

	/**
	 * Execucoes ainda ativas (nao terminais), ordenadas pela posicao na fila.
	 */
	List<Execucao> listarFila();

	long proximaPosicaoFila();
}
