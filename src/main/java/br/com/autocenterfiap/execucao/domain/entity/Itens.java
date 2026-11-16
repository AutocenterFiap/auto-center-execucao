package br.com.autocenterfiap.execucao.domain.entity;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Itens do diagnostico (servicos e produtos), com valores monetarios em
 * {@link BigDecimal} no dominio.
 */
@Getter
public class Itens {

	private final List<ItemServico> servicos;
	private final List<ItemProduto> produtos;

	public Itens(List<ItemServico> servicos, List<ItemProduto> produtos) {
		this.servicos = servicos == null ? List.of() : List.copyOf(servicos);
		this.produtos = produtos == null ? List.of() : List.copyOf(produtos);
	}

	public boolean vazio() {
		return servicos.isEmpty() && produtos.isEmpty();
	}

	public record ItemServico(Long servicoId, String descricao, BigDecimal valor) {
	}

	public record ItemProduto(Long produtoId, String descricao, int quantidade, BigDecimal precoUnitario) {
	}
}
