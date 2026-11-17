package br.com.autocenterfiap.execucao.application.port.out;

import java.math.BigDecimal;

/**
 * Catalogo de servicos e produtos do Cadastro Service. Item inexistente lanca
 * {@code ItemCatalogoInvalidoException}; falha de comunicacao lanca {@code CatalogoIndisponivelException}.
 */
public interface CatalogoPort {

	ServicoCatalogo buscarServico(Long id);

	ProdutoCatalogo buscarProduto(Long id);

	record ServicoCatalogo(Long id, String descricao, BigDecimal valor, boolean ativo) {
	}

	record ProdutoCatalogo(Long id, String nome, BigDecimal precoUnitario, boolean ativo) {
	}
}
