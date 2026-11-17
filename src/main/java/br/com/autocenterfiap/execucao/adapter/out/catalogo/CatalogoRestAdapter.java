package br.com.autocenterfiap.execucao.adapter.out.catalogo;

import br.com.autocenterfiap.execucao.application.exception.CatalogoIndisponivelException;
import br.com.autocenterfiap.execucao.application.exception.ItemCatalogoInvalidoException;
import br.com.autocenterfiap.execucao.application.port.out.CatalogoPort;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Consulta servicos e produtos no Cadastro Service por REST. */
@Component
public class CatalogoRestAdapter implements CatalogoPort {

	private final RestClient cliente;

	public CatalogoRestAdapter(@Qualifier("catalogoRestClient") RestClient cliente) {
		this.cliente = cliente;
	}

	@Override
	public ServicoCatalogo buscarServico(Long id) {
		var s = buscar("/v1/servicos/{id}", id, ServicoResposta.class, "Servico");
		return new ServicoCatalogo(s.id(), s.descricao(), s.valor(), "ATIVO".equals(s.status()));
	}

	@Override
	public ProdutoCatalogo buscarProduto(Long id) {
		var p = buscar("/v1/produtos/{id}", id, ProdutoResposta.class, "Produto");
		return new ProdutoCatalogo(p.id(), p.nome(), p.precoUnitario(), Boolean.TRUE.equals(p.ativo()));
	}

	private <T> T buscar(String caminho, Long id, Class<T> tipo, String recurso) {
		try {
			return cliente.get().uri(caminho, id).retrieve().body(tipo);
		} catch (HttpClientErrorException.NotFound e) {
			throw new ItemCatalogoInvalidoException(recurso + " " + id + " nao encontrado no catalogo");
		} catch (RestClientException e) {
			throw new CatalogoIndisponivelException("Cadastro indisponivel ao consultar " + recurso + " " + id, e);
		}
	}

	record ServicoResposta(Long id, String descricao, String status, BigDecimal valor) {
	}

	record ProdutoResposta(Long id, String nome, BigDecimal precoUnitario, Boolean ativo) {
	}
}
