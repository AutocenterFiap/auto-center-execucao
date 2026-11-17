package br.com.autocenterfiap.execucao.adapter.out.catalogo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import br.com.autocenterfiap.execucao.application.exception.CatalogoIndisponivelException;
import br.com.autocenterfiap.execucao.application.exception.ItemCatalogoInvalidoException;
import br.com.autocenterfiap.execucao.application.port.out.CatalogoPort.ProdutoCatalogo;
import br.com.autocenterfiap.execucao.application.port.out.CatalogoPort.ServicoCatalogo;
import br.com.autocenterfiap.execucao.infrastructure.config.RepassaTokenInterceptor;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class CatalogoRestAdapterTest {

	private MockRestServiceServer servidor;
	private CatalogoRestAdapter adapter;

	@BeforeEach
	void preparar() {
		var builder = RestClient.builder().baseUrl("http://cadastro").requestInterceptor(new RepassaTokenInterceptor());
		servidor = MockRestServiceServer.bindTo(builder).build();
		adapter = new CatalogoRestAdapter(builder.build());
	}

	@AfterEach
	void limpar() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	void servico200Mapeia() {
		servidor.expect(requestTo("http://cadastro/v1/servicos/1")).andRespond(withSuccess(
				"{\"id\":1,\"descricao\":\"Troca de oleo\",\"status\":\"ATIVO\",\"valor\":120.00}", MediaType.APPLICATION_JSON));

		assertThat(adapter.buscarServico(1L))
				.isEqualTo(new ServicoCatalogo(1L, "Troca de oleo", new BigDecimal("120.00"), true));
	}

	@Test
	void servicoInativoMapeiaAtivoFalse() {
		servidor.expect(requestTo("http://cadastro/v1/servicos/1")).andRespond(withSuccess(
				"{\"id\":1,\"descricao\":\"Troca\",\"status\":\"INATIVO\",\"valor\":1.00}", MediaType.APPLICATION_JSON));

		assertThat(adapter.buscarServico(1L).ativo()).isFalse();
	}

	@Test
	void produto200Mapeia() {
		servidor.expect(requestTo("http://cadastro/v1/produtos/2")).andRespond(withSuccess(
				"{\"id\":2,\"nome\":\"Oleo 5W30\",\"codigo\":\"P-2\",\"precoUnitario\":45.50,\"ativo\":true}",
				MediaType.APPLICATION_JSON));

		assertThat(adapter.buscarProduto(2L))
				.isEqualTo(new ProdutoCatalogo(2L, "Oleo 5W30", new BigDecimal("45.50"), true));
	}

	@Test
	void naoEncontrado404LancaInvalido() {
		servidor.expect(requestTo("http://cadastro/v1/servicos/9")).andRespond(withResourceNotFound());

		assertThatThrownBy(() -> adapter.buscarServico(9L))
				.isInstanceOf(ItemCatalogoInvalidoException.class)
				.hasMessage("Servico 9 nao encontrado no catalogo");
	}

	@Test
	void produto404LancaInvalido() {
		servidor.expect(requestTo("http://cadastro/v1/produtos/9")).andRespond(withResourceNotFound());

		assertThatThrownBy(() -> adapter.buscarProduto(9L))
				.isInstanceOf(ItemCatalogoInvalidoException.class)
				.hasMessage("Produto 9 nao encontrado no catalogo");
	}

	@Test
	void erro500LancaIndisponivel() {
		servidor.expect(requestTo("http://cadastro/v1/produtos/2")).andRespond(withServerError());

		assertThatThrownBy(() -> adapter.buscarProduto(2L)).isInstanceOf(CatalogoIndisponivelException.class);
	}

	@Test
	void repassaAuthorization() {
		var entrada = new MockHttpServletRequest();
		entrada.addHeader("Authorization", "Bearer x");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(entrada));
		servidor.expect(requestTo("http://cadastro/v1/servicos/1")).andExpect(header("Authorization", "Bearer x"))
				.andRespond(withSuccess("{\"id\":1,\"descricao\":\"T\",\"status\":\"ATIVO\",\"valor\":1.00}",
						MediaType.APPLICATION_JSON));

		adapter.buscarServico(1L);
		servidor.verify();
	}
}
