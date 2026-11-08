package br.com.autocenterfiap.execucao.adapter.messaging;

import br.com.autocenterfiap.execucao.adapter.messaging.contract.ItensContrato;
import br.com.autocenterfiap.execucao.domain.entity.Itens;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Converte os itens do dominio ({@link Itens}, com {@link BigDecimal}) para o formato
 * do contrato ({@link ItensContrato}, com valores em string de 2 casas).
 */
public final class ItensContratoMapper {

	private ItensContratoMapper() {
	}

	public static ItensContrato toContrato(Itens itens) {
		if (itens == null) {
			return new ItensContrato(List.of(), List.of());
		}
		List<ItensContrato.Servico> servicos = itens.getServicos().stream()
				.map(s -> new ItensContrato.Servico(s.servicoId(), s.descricao(), valor(s.valor())))
				.toList();
		List<ItensContrato.Produto> produtos = itens.getProdutos().stream()
				.map(p -> new ItensContrato.Produto(p.produtoId(), p.descricao(),
						p.quantidade(), valor(p.precoUnitario())))
				.toList();
		return new ItensContrato(servicos, produtos);
	}

	private static String valor(BigDecimal valor) {
		return valor == null ? null : valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
	}
}
