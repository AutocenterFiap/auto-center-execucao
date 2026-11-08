package br.com.autocenterfiap.execucao.adapter.out.persistence.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Representacao persistente (embutida) dos itens do diagnostico.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItensDocument {

	private List<ServicoDocument> servicos;
	private List<ProdutoDocument> produtos;

	@Getter
	@Setter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ServicoDocument {
		private Long servicoId;
		private String descricao;
		private BigDecimal valor;
	}

	@Getter
	@Setter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ProdutoDocument {
		private Long produtoId;
		private String descricao;
		private int quantidade;
		private BigDecimal precoUnitario;
	}
}
