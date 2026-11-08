package br.com.autocenterfiap.execucao.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusExecucaoTest {

	@Test
	void transicoesValidasDoFluxoFeliz() {
		assertTrue(StatusExecucao.NA_FILA.podeTransitarPara(StatusExecucao.EM_DIAGNOSTICO));
		assertTrue(StatusExecucao.EM_DIAGNOSTICO.podeTransitarPara(StatusExecucao.DIAGNOSTICO_CONCLUIDO));
		assertTrue(StatusExecucao.DIAGNOSTICO_CONCLUIDO.podeTransitarPara(StatusExecucao.EM_EXECUCAO));
		assertTrue(StatusExecucao.EM_EXECUCAO.podeTransitarPara(StatusExecucao.FINALIZADA));
		assertTrue(StatusExecucao.EM_EXECUCAO.podeTransitarPara(StatusExecucao.FALHA));
	}

	@Test
	void estadosAtivosPodemCancelar() {
		assertTrue(StatusExecucao.NA_FILA.podeTransitarPara(StatusExecucao.CANCELADA));
		assertTrue(StatusExecucao.EM_DIAGNOSTICO.podeTransitarPara(StatusExecucao.CANCELADA));
		assertTrue(StatusExecucao.DIAGNOSTICO_CONCLUIDO.podeTransitarPara(StatusExecucao.CANCELADA));
		assertTrue(StatusExecucao.EM_EXECUCAO.podeTransitarPara(StatusExecucao.CANCELADA));
	}

	@Test
	void transicoesInvalidas() {
		assertFalse(StatusExecucao.NA_FILA.podeTransitarPara(StatusExecucao.EM_EXECUCAO));
		assertFalse(StatusExecucao.NA_FILA.podeTransitarPara(StatusExecucao.FINALIZADA));
		assertFalse(StatusExecucao.EM_DIAGNOSTICO.podeTransitarPara(StatusExecucao.FINALIZADA));
		assertFalse(StatusExecucao.EM_DIAGNOSTICO.podeTransitarPara(StatusExecucao.FALHA));
	}

	@Test
	void estadosTerminais() {
		assertTrue(StatusExecucao.FINALIZADA.ehTerminal());
		assertTrue(StatusExecucao.FALHA.ehTerminal());
		assertTrue(StatusExecucao.CANCELADA.ehTerminal());
		assertFalse(StatusExecucao.NA_FILA.ehTerminal());
		assertTrue(StatusExecucao.CANCELADA.transicoesValidas().isEmpty());
	}
}
