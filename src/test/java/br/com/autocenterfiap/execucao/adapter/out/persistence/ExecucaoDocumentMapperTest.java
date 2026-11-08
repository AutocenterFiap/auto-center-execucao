package br.com.autocenterfiap.execucao.adapter.out.persistence;

import br.com.autocenterfiap.execucao.adapter.out.persistence.document.ExecucaoDocument;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;
import br.com.autocenterfiap.execucao.domain.entity.Itens;
import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExecucaoDocumentMapperTest {

	@Test
	void roundTripDevePreservarEstadoItensEHistorico() {
		Execucao origem = Execucao.criar(123L, 1001L, 42L, 77L, "Barulho", "corr-1", 2);
		origem.iniciarDiagnostico(3L);
		origem.concluirDiagnostico(new Itens(
				List.of(new Itens.ItemServico(1L, "Troca de óleo", new BigDecimal("120.00"))),
				List.of(new Itens.ItemProduto(7L, "Filtro", 1, new BigDecimal("45.00")))));

		ExecucaoDocument document = ExecucaoDocumentMapper.toDocument(origem);

		assertEquals(123L, document.getOsId());
		assertEquals(3L, document.getMecanicoId());
		assertEquals(StatusExecucao.DIAGNOSTICO_CONCLUIDO, document.getStatus());
		assertEquals(new BigDecimal("120.00"), document.getItens().getServicos().get(0).getValor());

		Execucao volta = ExecucaoDocumentMapper.toDomain(document);

		assertEquals(123L, volta.getOsId());
		assertEquals(1001L, volta.getNumero());
		assertEquals(3L, volta.getMecanicoId());
		assertEquals(StatusExecucao.DIAGNOSTICO_CONCLUIDO, volta.getStatus());
		assertEquals(new BigDecimal("45.00"), volta.getItens().getProdutos().get(0).precoUnitario());
		assertEquals(3, volta.getHistorico().size());
	}
}
