package br.com.autocenterfiap.execucao.adapter.in.controller;

import br.com.autocenterfiap.execucao.application.exception.ExecucaoNaoEncontradaException;
import br.com.autocenterfiap.execucao.application.port.in.ConsultarExecucaoUseCase;
import br.com.autocenterfiap.execucao.application.port.in.GerenciarExecucaoUseCase;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;
import br.com.autocenterfiap.execucao.domain.entity.Itens;
import br.com.autocenterfiap.execucao.domain.exception.TransicaoInvalidaException;
import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
class ExecucaoControllerTest {

	@Autowired
	MockMvc mockMvc;
	@MockBean
	GerenciarExecucaoUseCase gerenciar;
	@MockBean
	ConsultarExecucaoUseCase consultar;

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
						.content("{\"servicos\":[{\"servicoId\":1,\"valor\":120.00}],\"produtos\":[]}"))
				.andExpect(status().isConflict());
	}

	@Test
	void falhaSemMotivoDeveRetornar400() throws Exception {
		mockMvc.perform(post("/execucoes/123/falha")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"motivo\":\"\"}"))
				.andExpect(status().isBadRequest());
	}
}
