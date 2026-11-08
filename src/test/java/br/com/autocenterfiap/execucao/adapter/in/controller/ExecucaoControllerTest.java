package br.com.autocenterfiap.execucao.adapter.in.controller;

import br.com.autocenterfiap.execucao.application.exception.ExecucaoNaoEncontradaException;
import br.com.autocenterfiap.execucao.application.port.in.ConsultarExecucaoUseCase;
import br.com.autocenterfiap.execucao.application.port.in.GerenciarExecucaoUseCase;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;
import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;
import br.com.autocenterfiap.execucao.domain.exception.TransicaoInvalidaException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;

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

	private Execucao execucaoEm(StatusExecucao status) {
		return Execucao.reconstituir("e1", "os-1", status, 1, null,
				Instant.now(), Instant.now(), null, new ArrayList<>());
	}

	@Test
	void porIdDeveRetornar200() throws Exception {
		when(consultar.buscarPorId("e1")).thenReturn(execucaoEm(StatusExecucao.NA_FILA));

		mockMvc.perform(get("/execucoes/e1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("e1"))
				.andExpect(jsonPath("$.status").value("NA_FILA"));
	}

	@Test
	void porIdInexistenteDeveRetornar404() throws Exception {
		when(consultar.buscarPorId("x")).thenThrow(new ExecucaoNaoEncontradaException("id", "x"));

		mockMvc.perform(get("/execucoes/x"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void finalizarDeveRetornar200() throws Exception {
		when(gerenciar.finalizar(eq("e1"), any())).thenReturn(execucaoEm(StatusExecucao.FINALIZADA));

		mockMvc.perform(post("/execucoes/e1/finalizacao"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("FINALIZADA"));
	}

	@Test
	void transicaoInvalidaDeveRetornar409() throws Exception {
		when(gerenciar.iniciarReparo(eq("e1"), any()))
				.thenThrow(new TransicaoInvalidaException(StatusExecucao.NA_FILA, StatusExecucao.EM_EXECUCAO));

		mockMvc.perform(post("/execucoes/e1/reparo"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void falhaSemMotivoDeveRetornar400() throws Exception {
		mockMvc.perform(post("/execucoes/e1/falha")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"motivo\":\"\"}"))
				.andExpect(status().isBadRequest());
	}
}
