package br.com.autocenterfiap.execucao.adapter.in.dto;

import br.com.autocenterfiap.execucao.application.dto.ItensSolicitados;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * Itens do diagnostico informados pela oficina: so IDs do catalogo e quantidade. Descricao e preco
 * vem do Cadastro Service; campos extras no JSON (ex.: descricao, valor) sao ignorados.
 */
@Schema(example = "{\"servicos\":[{\"servicoId\":1}],\"produtos\":[{\"produtoId\":2,\"quantidade\":3}]}")
public record ItensRequest(List<@Valid @NotNull ServicoRequest> servicos,
		List<@Valid @NotNull ProdutoRequest> produtos) {

	public record ServicoRequest(@NotNull Long servicoId) {
	}

	public record ProdutoRequest(@NotNull Long produtoId, @NotNull @Positive Integer quantidade) {
	}

	public ItensSolicitados toSolicitados() {
		var ids = (servicos == null ? List.<ServicoRequest>of() : servicos).stream()
				.map(ServicoRequest::servicoId)
				.toList();
		var itensProduto = (produtos == null ? List.<ProdutoRequest>of() : produtos).stream()
				.map(p -> new ItensSolicitados.ProdutoSolicitado(p.produtoId(), p.quantidade()))
				.toList();
		return new ItensSolicitados(ids, itensProduto);
	}
}
