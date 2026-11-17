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
@Schema(description = "Itens do diagnóstico identificados apenas por ID; descrição e preço vêm do Cadastro",
		example = "{\"servicos\":[{\"servicoId\":1}],\"produtos\":[{\"produtoId\":2,\"quantidade\":3}]}")
public record ItensRequest(
		@Schema(description = "Serviços a executar") List<@Valid @NotNull ServicoRequest> servicos,
		@Schema(description = "Produtos e insumos a utilizar") List<@Valid @NotNull ProdutoRequest> produtos) {

	@Schema(name = "ServicoDiagnosticoRequest", description = "Serviço do catálogo")
	public record ServicoRequest(
			@Schema(description = "Identificador do serviço no Cadastro", example = "1",
					requiredMode = Schema.RequiredMode.REQUIRED) @NotNull Long servicoId) {
	}

	@Schema(name = "ProdutoDiagnosticoRequest", description = "Produto do catálogo e quantidade")
	public record ProdutoRequest(
			@Schema(description = "Identificador do produto no Cadastro", example = "2",
					requiredMode = Schema.RequiredMode.REQUIRED) @NotNull Long produtoId,
			@Schema(description = "Quantidade a utilizar", example = "3", minimum = "1",
					requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Positive Integer quantidade) {
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
