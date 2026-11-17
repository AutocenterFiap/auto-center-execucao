package br.com.autocenterfiap.execucao.adapter.in.controller;

import br.com.autocenterfiap.execucao.application.exception.ExecucaoNaoEncontradaException;
import br.com.autocenterfiap.execucao.application.exception.CatalogoIndisponivelException;
import br.com.autocenterfiap.execucao.application.exception.ItemCatalogoInvalidoException;
import br.com.autocenterfiap.execucao.application.port.out.CatalogoPort;
import br.com.autocenterfiap.execucao.application.port.out.CatalogoPort.ProdutoCatalogo;
import br.com.autocenterfiap.execucao.application.port.out.CatalogoPort.ServicoCatalogo;
import br.com.autocenterfiap.execucao.application.usecase.MontarItensCatalogoService;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import br.com.autocenterfiap.execucao.application.port.in.ConsultarExecucaoUseCase;
import br.com.autocenterfiap.execucao.application.port.in.GerenciarExecucaoUseCase;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;
import br.com.autocenterfiap.execucao.domain.entity.Itens;
import br.com.autocenterfiap.execucao.domain.exception.TransicaoInvalidaException;
import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExecucaoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ExecucaoControllerTest.Config.class)
class ExecucaoControllerTest {

	@TestConfiguration
	static class Config {
		@Bean
		MontarItensCatalogoService montarItensCatalogoService(CatalogoPort catalogo) {
			return new MontarItensCatalogoService(catalogo);
		}
	}

	@Autowired
	MockMvc mockMvc;
	@MockBean
	GerenciarExecucaoUseCase gerenciar;
	@MockBean
	ConsultarExecucaoUseCase consultar;
	@MockBean
	CatalogoPort catalogo;

	private Execucao naFila() {
		return Execucao.criar(123L, 1001L, 42L, 77L, "Barulho", "corr-1", 1);
	}

	private Execucao finalizada() {
		Execucao e = naFila();
		e.iniciarDiagnostico(3L);
		e.concluirDiagnostico(new Itens(List.of(), List.of()));
		e.iniciarReparo(null);
		e.finalizar();
		return e;
	}

	@Test
	void porOsIdDeveRetornar200() throws Exception {
		when(consultar.buscarPorOsId(123L)).thenReturn(naFila());

		mockMvc.perform(get("/execucoes/os/123"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.osId").value(123))
				.andExpect(jsonPath("$.status").value("NA_FILA"));
	}

	@Test
	void porOsIdInexistenteDeveRetornar404() throws Exception {
		when(consultar.buscarPorOsId(999L)).thenThrow(new ExecucaoNaoEncontradaException(999L));

		mockMvc.perform(get("/execucoes/os/999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void finalizarDeveRetornar200() throws Exception {
		when(gerenciar.finalizar(123L)).thenReturn(finalizada());

		mockMvc.perform(post("/execucoes/123/finalizacao"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("FINALIZADA"));
	}

	@Test
	void concluirDiagnosticoComTransicaoInvalidaDeveRetornar409() throws Exception {
		when(gerenciar.concluirDiagnostico(eq(123L), any()))
				.thenThrow(new TransicaoInvalidaException(StatusExecucao.NA_FILA, StatusExecucao.DIAGNOSTICO_CONCLUIDO));

		mockMvc.perform(post("/execucoes/123/diagnostico")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"servicos\":[],\"produtos\":[]}"))
				.andExpect(status().isConflict());
	}

	@Test
	void concluirDiagnosticoComItemForaDoContratoDeveRetornar400() throws Exception {
		String[] invalidos = {
				"{\"servicos\":[{}],\"produtos\":[]}",
				"{\"servicos\":[],\"produtos\":[{\"produtoId\":7}]}",
				"{\"servicos\":[],\"produtos\":[{\"quantidade\":1}]}",
				"{\"servicos\":[],\"produtos\":[{\"produtoId\":7,\"quantidade\":0}]}"
		};
		for (String corpo : invalidos) {
			mockMvc.perform(post("/execucoes/123/diagnostico")
							.contentType(MediaType.APPLICATION_JSON)
							.content(corpo))
					.andExpect(status().isBadRequest());
		}
		verifyNoInteractions(catalogo, gerenciar);
	}

	@Test
	void concluirDiagnosticoSoComIdsUsaPrecoDoCatalogo() throws Exception {
		when(catalogo.buscarServico(1L)).thenReturn(new ServicoCatalogo(1L, "Troca de oleo", new BigDecimal("120.00"), true));
		when(catalogo.buscarProduto(2L)).thenReturn(new ProdutoCatalogo(2L, "Oleo 5W30", new BigDecimal("45.50"), true));
		Execucao diagnosticada = naFila();
		diagnosticada.iniciarDiagnostico(3L);
		when(gerenciar.concluirDiagnostico(eq(123L), any())).thenAnswer(inv -> {
			diagnosticada.concluirDiagnostico(inv.getArgument(1));
			return diagnosticada;
		});

		mockMvc.perform(post("/execucoes/123/diagnostico")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"servicos\":[{\"servicoId\":1,\"descricao\":\"ignorada\",\"valor\":1}],"
								+ "\"produtos\":[{\"produtoId\":2,\"quantidade\":3}]}"))
				.andExpect(status().isOk());

		ArgumentCaptor<Itens> itens = ArgumentCaptor.forClass(Itens.class);
		verify(gerenciar).concluirDiagnostico(eq(123L), itens.capture());
		assertThat(itens.getValue().getServicos())
				.containsExactly(new Itens.ItemServico(1L, "Troca de oleo", new BigDecimal("120.00")));
		assertThat(itens.getValue().getProdutos())
				.containsExactly(new Itens.ItemProduto(2L, "Oleo 5W30", 3, new BigDecimal("45.50")));
	}

	@Test
	void itemInexistenteRetorna422ENaoConclui() throws Exception {
		when(catalogo.buscarServico(9L)).thenThrow(new ItemCatalogoInvalidoException("Servico 9 nao encontrado no catalogo"));

		mockMvc.perform(post("/execucoes/123/diagnostico")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"servicos\":[{\"servicoId\":9}],\"produtos\":[]}"))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.mensagem").value("Servico 9 nao encontrado no catalogo"));
		verifyNoInteractions(gerenciar);
	}

	@Test
	void catalogoForaRetorna503ENaoConclui() throws Exception {
		when(catalogo.buscarServico(1L)).thenThrow(new CatalogoIndisponivelException("fora", new RuntimeException()));

		mockMvc.perform(post("/execucoes/123/diagnostico")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"servicos\":[{\"servicoId\":1}],\"produtos\":[]}"))
				.andExpect(status().isServiceUnavailable());
		verifyNoInteractions(gerenciar);
	}

	@Test
	void falhaSemMotivoDeveRetornar400() throws Exception {
		mockMvc.perform(post("/execucoes/123/falha")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"motivo\":\"\"}"))
				.andExpect(status().isBadRequest());
	}
}
