package br.com.autocenterfiap.execucao.application.usecase;

import br.com.autocenterfiap.execucao.application.dto.ItensSolicitados;
import br.com.autocenterfiap.execucao.application.exception.ItemCatalogoInvalidoException;
import br.com.autocenterfiap.execucao.application.port.out.CatalogoPort;
import br.com.autocenterfiap.execucao.domain.entity.Itens;

/** Monta os itens do diagnostico com descricao e preco do catalogo, recusando itens inativos. */
public class MontarItensCatalogoService {

	private final CatalogoPort catalogo;

	public MontarItensCatalogoService(CatalogoPort catalogo) {
		this.catalogo = catalogo;
	}

	public Itens montar(ItensSolicitados solicitados) {
		var servicos = solicitados.servicoIds().stream().map(this::servico).toList();
		var produtos = solicitados.produtos().stream().map(this::produto).toList();
		return new Itens(servicos, produtos);
	}

	private Itens.ItemServico servico(Long id) {
		var s = catalogo.buscarServico(id);
		if (!s.ativo()) {
			throw new ItemCatalogoInvalidoException("Servico " + id + " inativo no catalogo");
		}
		return new Itens.ItemServico(s.id(), s.descricao(), s.valor());
	}

	private Itens.ItemProduto produto(ItensSolicitados.ProdutoSolicitado solicitado) {
		var p = catalogo.buscarProduto(solicitado.produtoId());
		if (!p.ativo()) {
			throw new ItemCatalogoInvalidoException("Produto " + solicitado.produtoId() + " inativo no catalogo");
		}
		return new Itens.ItemProduto(p.id(), p.nome(), solicitado.quantidade(), p.precoUnitario());
	}
}
