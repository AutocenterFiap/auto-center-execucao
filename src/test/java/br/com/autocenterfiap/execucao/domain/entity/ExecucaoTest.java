package br.com.autocenterfiap.execucao.domain.entity;

import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;
import br.com.autocenterfiap.execucao.domain.exception.ExecucaoInvalidaException;
import br.com.autocenterfiap.execucao.domain.exception.TransicaoInvalidaException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecucaoTest {

	private Execucao novaExecucao() {
		return Execucao.criar(123L, 1001L, 42L, 77L, "Barulho ao frear", "corr-1", 1);
	}

	private Itens itens() {
		return new Itens(
				List.of(new Itens.ItemServico(1L, "Troca de óleo", new BigDecimal("120.00"))),
				List.of(new Itens.ItemProduto(7L, "Filtro", 1, new BigDecimal("45.00"))));
	}

	@Test
	void criarDeveIniciarNaFilaComOsDados() {
		Execucao e = novaExecucao();
		assertEquals(123L, e.getOsId());
		assertEquals(1001L, e.getNumero());
		assertEquals(42L, e.getClienteId());
		assertEquals(StatusExecucao.NA_FILA, e.getStatus());
		assertEquals(1, e.getPosicaoFila());
		assertEquals(1, e.getHistorico().size());
	}

	@Test
	void criarDeveRejeitarOsIdNulo() {
		assertThrows(ExecucaoInvalidaException.class,
				() -> Execucao.criar(null, 1L, 1L, 1L, "x", "c", 1));
	}

	@Test
	void fluxoFeliz() {
		Execucao e = novaExecucao();
		e.iniciarDiagnostico(3L);
		assertEquals(StatusExecucao.EM_DIAGNOSTICO, e.getStatus());
		assertEquals(3L, e.getMecanicoId());

		e.concluirDiagnostico(itens());
		assertEquals(StatusExecucao.DIAGNOSTICO_CONCLUIDO, e.getStatus());
		assertFalse(e.getItens().vazio());

		e.iniciarReparo(null);
		assertEquals(StatusExecucao.EM_EXECUCAO, e.getStatus());
		assertEquals(3L, e.getMecanicoId()); // mantem o mecanico do diagnostico

		e.finalizar();
		assertEquals(StatusExecucao.FINALIZADA, e.getStatus());
		assertNotNull(e.getFinalizadoEm());
		assertEquals(5, e.getHistorico().size());
	}

	@Test
	void transicaoInvalidaNaoAlteraEstado() {
		Execucao e = novaExecucao();
		assertThrows(TransicaoInvalidaException.class, () -> e.iniciarReparo(null));
		assertEquals(StatusExecucao.NA_FILA, e.getStatus());
	}

	@Test
	void falharNoReparo() {
		Execucao e = novaExecucao();
		e.iniciarDiagnostico(3L);
		e.concluirDiagnostico(itens());
		e.iniciarReparo(null);

		e.falhar("Peça com defeito");

		assertEquals(StatusExecucao.FALHA, e.getStatus());
		assertEquals("Peça com defeito", e.getMotivo());
		assertNotNull(e.getFinalizadoEm());
	}

	@Test
	void cancelarAPartirDeEstadoAtivo() {
		Execucao e = novaExecucao();
		e.iniciarDiagnostico(3L);

		assertTrue(e.cancelar("ORCAMENTO_REPROVADO"));
		assertEquals(StatusExecucao.CANCELADA, e.getStatus());
		assertEquals("ORCAMENTO_REPROVADO", e.getMotivo());
	}

	@Test
	void cancelarEmEstadoTerminalEhNoOp() {
		Execucao e = novaExecucao();
		e.iniciarDiagnostico(3L);
		e.concluirDiagnostico(itens());
		e.iniciarReparo(null);
		e.finalizar();

		assertFalse(e.cancelar("tarde demais"));
		assertEquals(StatusExecucao.FINALIZADA, e.getStatus());
	}

	@Test
	void historicoImutavel() {
		Execucao e = novaExecucao();
		assertThrows(UnsupportedOperationException.class, () -> e.getHistorico().add(null));
	}
}
