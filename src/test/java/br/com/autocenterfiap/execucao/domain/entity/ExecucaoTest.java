package br.com.autocenterfiap.execucao.domain.entity;

import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;
import br.com.autocenterfiap.execucao.domain.exception.ExecucaoInvalidaException;
import br.com.autocenterfiap.execucao.domain.exception.TransicaoInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecucaoTest {

	@Test
	@DisplayName("criarNaFila deve iniciar em NA_FILA e registrar o primeiro evento")
	void criarNaFila() {
		Execucao execucao = Execucao.criarNaFila("os-123", 1);

		assertEquals("os-123", execucao.getOsId());
		assertEquals(StatusExecucao.NA_FILA, execucao.getStatus());
		assertEquals(1, execucao.getPosicaoFila());
		assertNotNull(execucao.getCriadoEm());
		assertNull(execucao.getFinalizadoEm());
		assertEquals(1, execucao.getHistorico().size());
		assertEquals(StatusExecucao.NA_FILA, execucao.getHistorico().get(0).getStatusNovo());
	}

	@Test
	@DisplayName("criarNaFila deve rejeitar osId em branco")
	void criarNaFilaOsIdInvalido() {
		assertThrows(ExecucaoInvalidaException.class, () -> Execucao.criarNaFila("  ", 1));
		assertThrows(ExecucaoInvalidaException.class, () -> Execucao.criarNaFila(null, 1));
	}

	@Test
	@DisplayName("fluxo feliz: NA_FILA -> EM_DIAGNOSTICO -> EM_EXECUCAO -> FINALIZADA")
	void fluxoFeliz() {
		Execucao execucao = Execucao.criarNaFila("os-123", 1);

		execucao.iniciarDiagnostico("avaliando o veiculo");
		assertEquals(StatusExecucao.EM_DIAGNOSTICO, execucao.getStatus());

		execucao.iniciarReparo("trocando pastilhas");
		assertEquals(StatusExecucao.EM_EXECUCAO, execucao.getStatus());

		execucao.finalizar("reparo concluido");
		assertEquals(StatusExecucao.FINALIZADA, execucao.getStatus());
		assertNotNull(execucao.getFinalizadoEm());
		assertTrue(execucao.getStatus().ehTerminal());
		// 1 (criacao) + 3 transicoes
		assertEquals(4, execucao.getHistorico().size());
	}

	@Test
	@DisplayName("transicao invalida deve lancar excecao e nao alterar o estado")
	void transicaoInvalida() {
		Execucao execucao = Execucao.criarNaFila("os-123", 1);

		// NA_FILA nao pode ir direto para EM_EXECUCAO (finalizar tambem invalido aqui)
		assertThrows(TransicaoInvalidaException.class, () -> execucao.iniciarReparo(null));
		assertThrows(TransicaoInvalidaException.class, () -> execucao.finalizar(null));
		assertEquals(StatusExecucao.NA_FILA, execucao.getStatus());
		assertEquals(1, execucao.getHistorico().size());
	}

	@Test
	@DisplayName("falhar deve registrar motivo e marcar estado terminal FALHA")
	void falhar() {
		Execucao execucao = Execucao.criarNaFila("os-123", 1);
		execucao.iniciarDiagnostico(null);

		execucao.falhar("peca indisponivel");

		assertEquals(StatusExecucao.FALHA, execucao.getStatus());
		assertEquals("peca indisponivel", execucao.getMotivoFalha());
		assertNotNull(execucao.getFinalizadoEm());
	}

	@Test
	@DisplayName("nao deve permitir transicao a partir de estado terminal")
	void transicaoAPartirDeTerminal() {
		Execucao execucao = Execucao.criarNaFila("os-123", 1);
		execucao.iniciarDiagnostico(null);
		execucao.iniciarReparo(null);
		execucao.finalizar(null);

		assertThrows(TransicaoInvalidaException.class, () -> execucao.falhar("tarde demais"));
	}

	@Test
	@DisplayName("getHistorico deve ser imutavel")
	void historicoImutavel() {
		Execucao execucao = Execucao.criarNaFila("os-123", 1);
		assertThrows(UnsupportedOperationException.class,
				() -> execucao.getHistorico().add(null));
	}

	@Test
	@DisplayName("reconstituir deve restaurar o estado sem gerar novos eventos")
	void reconstituir() {
		Execucao original = Execucao.criarNaFila("os-123", 2);
		original.iniciarDiagnostico(null);

		Execucao restaurada = Execucao.reconstituir(
				"abc-id", original.getOsId(), original.getStatus(), original.getPosicaoFila(),
				null, original.getCriadoEm(), original.getAtualizadoEm(), null, original.getHistorico());

		assertEquals("abc-id", restaurada.getId());
		assertEquals(StatusExecucao.EM_DIAGNOSTICO, restaurada.getStatus());
		assertEquals(2, restaurada.getHistorico().size());
	}

	@Test
	@DisplayName("reconstituir deve validar campos obrigatorios")
	void reconstituirInvalido() {
		assertThrows(ExecucaoInvalidaException.class, () -> Execucao.reconstituir(
				"id", null, StatusExecucao.NA_FILA, 1, null, null, null, null, null));
		assertThrows(ExecucaoInvalidaException.class, () -> Execucao.reconstituir(
				"id", "os-1", null, 1, null, null, null, null, null));
	}
}
