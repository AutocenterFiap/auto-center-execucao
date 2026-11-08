package br.com.autocenterfiap.execucao.adapter.out.persistence;

import br.com.autocenterfiap.execucao.adapter.out.persistence.document.ExecucaoDocument;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;
import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExecucaoDocumentMapperTest {

	@Test
	void roundTripDeveraPreservarEstadoEHistorico() {
		Execucao origem = Execucao.criarNaFila("os-1", 2);
		origem.iniciarDiagnostico("avaliando");
		origem.iniciarReparo(null);

		ExecucaoDocument document = ExecucaoDocumentMapper.toDocument(origem);

		assertEquals("os-1", document.getOsId());
		assertEquals(StatusExecucao.EM_EXECUCAO, document.getStatus());
		assertEquals(2, document.getPosicaoFila());
		assertEquals(3, document.getHistorico().size());

		Execucao volta = ExecucaoDocumentMapper.toDomain(document);

		assertEquals("os-1", volta.getOsId());
		assertEquals(StatusExecucao.EM_EXECUCAO, volta.getStatus());
		assertEquals(2, volta.getPosicaoFila());
		assertEquals(3, volta.getHistorico().size());
		assertEquals(StatusExecucao.EM_DIAGNOSTICO, volta.getHistorico().get(1).getStatusNovo());
	}

	@Test
	void toDomainDeveTolerarHistoricoNulo() {
		ExecucaoDocument document = ExecucaoDocument.builder()
				.id("e1")
				.osId("os-1")
				.status(StatusExecucao.NA_FILA)
				.posicaoFila(1)
				.historico(null)
				.build();

		Execucao domain = ExecucaoDocumentMapper.toDomain(document);

		assertEquals("e1", domain.getId());
		assertEquals(0, domain.getHistorico().size());
		assertNull(domain.getFinalizadoEm());
	}
}
