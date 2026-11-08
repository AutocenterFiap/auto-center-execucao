package br.com.autocenterfiap.execucao.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusExecucaoTest {

	@Test
	void transicoesValidasDoFluxoFeliz() {
		assertTrue(StatusExecucao.NA_FILA.podeTransitarPara(StatusExecucao.EM_DIAGNOSTICO));
		assertTrue(StatusExecucao.EM_DIAGNOSTICO.podeTransitarPara(StatusExecucao.EM_EXECUCAO));
		assertTrue(StatusExecucao.EM_EXECUCAO.podeTransitarPara(StatusExecucao.FINALIZADA));
	}

	@Test
	void qualquerEstadoAtivoPodeFalhar() {
		assertTrue(StatusExecucao.NA_FILA.podeTransitarPara(StatusExecucao.FALHA));
		assertTrue(StatusExecucao.EM_DIAGNOSTICO.podeTransitarPara(StatusExecucao.FALHA));
		assertTrue(StatusExecucao.EM_EXECUCAO.podeTransitarPara(StatusExecucao.FALHA));
	}

	@Test
	void transicoesInvalidas() {
		assertFalse(StatusExecucao.NA_FILA.podeTransitarPara(StatusExecucao.EM_EXECUCAO));
		assertFalse(StatusExecucao.NA_FILA.podeTransitarPara(StatusExecucao.FINALIZADA));
		assertFalse(StatusExecucao.EM_DIAGNOSTICO.podeTransitarPara(StatusExecucao.FINALIZADA));
	}

	@Test
	void estadosTerminaisNaoTransitam() {
		assertTrue(StatusExecucao.FINALIZADA.transicoesValidas().isEmpty());
		assertTrue(StatusExecucao.FALHA.transicoesValidas().isEmpty());
		assertTrue(StatusExecucao.FINALIZADA.ehTerminal());
		assertTrue(StatusExecucao.FALHA.ehTerminal());
		assertFalse(StatusExecucao.NA_FILA.ehTerminal());
	}
}
