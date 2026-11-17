package br.com.autocenterfiap.execucao.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.com.autocenterfiap.execucao.application.dto.ItensSolicitados;
import br.com.autocenterfiap.execucao.application.exception.ItemCatalogoInvalidoException;
import br.com.autocenterfiap.execucao.application.port.out.CatalogoPort;
import br.com.autocenterfiap.execucao.application.port.out.CatalogoPort.ProdutoCatalogo;
import br.com.autocenterfiap.execucao.application.port.out.CatalogoPort.ServicoCatalogo;
import br.com.autocenterfiap.execucao.domain.entity.Itens;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class MontarItensCatalogoServiceTest {

	private final CatalogoPort catalogo = mock(CatalogoPort.class);
	private final MontarItensCatalogoService montar = new MontarItensCatalogoService(catalogo);

	@Test
	void usaDescricaoEPrecoDoCatalogo() {
		when(catalogo.buscarServico(1L)).thenReturn(new ServicoCatalogo(1L, "Troca de oleo", new BigDecimal("120.00"), true));
		when(catalogo.buscarProduto(2L)).thenReturn(new ProdutoCatalogo(2L, "Oleo 5W30", new BigDecimal("45.50"), true));

		Itens itens = montar.montar(new ItensSolicitados(List.of(1L),
				List.of(new ItensSolicitados.ProdutoSolicitado(2L, 3))));

		assertThat(itens.getServicos()).containsExactly(
				new Itens.ItemServico(1L, "Troca de oleo", new BigDecimal("120.00")));
		assertThat(itens.getProdutos()).containsExactly(
				new Itens.ItemProduto(2L, "Oleo 5W30", 3, new BigDecimal("45.50")));
	}

	@Test
	void servicoInativoLancaInvalido() {
		when(catalogo.buscarServico(1L)).thenReturn(new ServicoCatalogo(1L, "Troca", BigDecimal.TEN, false));

		assertThatThrownBy(() -> montar.montar(new ItensSolicitados(List.of(1L), List.of())))
				.isInstanceOf(ItemCatalogoInvalidoException.class)
				.hasMessage("Servico 1 inativo no catalogo");
	}

	@Test
	void produtoInativoLancaInvalido() {
		when(catalogo.buscarProduto(2L)).thenReturn(new ProdutoCatalogo(2L, "Oleo", BigDecimal.TEN, false));

		assertThatThrownBy(() -> montar.montar(new ItensSolicitados(List.of(),
				List.of(new ItensSolicitados.ProdutoSolicitado(2L, 1)))))
				.isInstanceOf(ItemCatalogoInvalidoException.class)
				.hasMessage("Produto 2 inativo no catalogo");
	}

	@Test
	void listasVaziasRetornamItensVazios() {
		Itens itens = montar.montar(new ItensSolicitados(List.of(), List.of()));

		assertThat(itens.vazio()).isTrue();
		verifyNoInteractions(catalogo);
	}
}
