package br.com.autocenterfiap.execucao.adapter.in.dto;

import br.com.autocenterfiap.execucao.domain.entity.Itens;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

/**
 * Itens do diagnostico informados pela oficina ao concluir o diagnostico.
 */
public record ItensRequest(List<@Valid @NotNull ServicoRequest> servicos,
		List<@Valid @NotNull ProdutoRequest> produtos) {

	public record ServicoRequest(@NotNull Long servicoId, @NotBlank String descricao,
	                             @NotNull @PositiveOrZero BigDecimal valor) {
	}

	public record ProdutoRequest(@NotNull Long produtoId, @NotBlank String descricao,
	                             @NotNull @Positive Integer quantidade,
	                             @NotNull @PositiveOrZero BigDecimal precoUnitario) {
	}

	public Itens toDomain() {
		List<Itens.ItemServico> itensServico = (servicos == null ? List.<ServicoRequest>of() : servicos).stream()
				.map(s -> new Itens.ItemServico(s.servicoId(), s.descricao(), s.valor()))
				.toList();
		List<Itens.ItemProduto> itensProduto = (produtos == null ? List.<ProdutoRequest>of() : produtos).stream()
				.map(p -> new Itens.ItemProduto(p.produtoId(), p.descricao(), p.quantidade(), p.precoUnitario()))
				.toList();
		return new Itens(itensServico, itensProduto);
	}
}
