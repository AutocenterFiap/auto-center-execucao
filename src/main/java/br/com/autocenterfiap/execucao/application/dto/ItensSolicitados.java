package br.com.autocenterfiap.execucao.application.dto;

import java.util.List;

/**
 * Itens que a oficina informa ao concluir o diagnostico: so os IDs do catalogo e as quantidades.
 * Descricao e preco vem do Cadastro.
 */
public record ItensSolicitados(List<Long> servicoIds, List<ProdutoSolicitado> produtos) {

	public ItensSolicitados {
		servicoIds = servicoIds == null ? List.of() : List.copyOf(servicoIds);
		produtos = produtos == null ? List.of() : List.copyOf(produtos);
	}

	public record ProdutoSolicitado(Long produtoId, int quantidade) {
	}
}
