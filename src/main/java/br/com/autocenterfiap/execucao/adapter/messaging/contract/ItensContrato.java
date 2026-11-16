package br.com.autocenterfiap.execucao.adapter.messaging.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Itens do diagnostico/reparo, no formato do contrato (valores monetarios como
 * strings com 2 casas, ex.: "120.00").
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ItensContrato(List<Servico> servicos, List<Produto> produtos) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Servico(Long servicoId, String descricao, String valor) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Produto(Long produtoId, String descricao, Integer quantidade, String precoUnitario) {
	}

	public boolean vazio() {
		return (servicos == null || servicos.isEmpty()) && (produtos == null || produtos.isEmpty());
	}
}
