package br.com.autocenterfiap.execucao.adapter.messaging.contract;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventoEnvelopeTest {

	private final ObjectMapper mapper = JsonMapper.builder()
			.addModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.serializationInclusion(JsonInclude.Include.NON_NULL)
			.build();

	@Test
	void deveDesserializarOsCriadaDoContrato() throws Exception {
		String json = """
				{
				  "eventId": "11111111-1111-1111-1111-111111111111",
				  "tipo": "os.criada",
				  "versao": 1,
				  "osId": 123,
				  "correlationId": "22222222-2222-2222-2222-222222222222",
				  "ocorridoEm": "2026-01-01T00:00:00Z",
				  "payload": { "numero": 1001, "clienteId": 42, "veiculoId": 77, "problemaRelatado": "Barulho ao frear" }
				}
				""";

		EventoEnvelope<ExecucaoPayloads.OsCriada> envelope =
				mapper.readValue(json, new TypeReference<>() {
				});

		assertEquals(TipoEvento.OS_CRIADA, envelope.tipo());
		assertEquals(123L, envelope.osId());
		assertEquals(1001L, envelope.payload().numero());
		assertEquals(42L, envelope.payload().clienteId());
		assertNotNull(envelope.ocorridoEm());
	}

	@Test
	void deveDesserializarDiagnosticoConcluidoComItensEIgnorarCamposDesconhecidos() throws Exception {
		String json = """
				{
				  "eventId": "11111111-1111-1111-1111-111111111111",
				  "tipo": "diagnostico.concluido",
				  "versao": 1,
				  "osId": 123,
				  "campoDesconhecido": "ignorar",
				  "correlationId": "22222222-2222-2222-2222-222222222222",
				  "ocorridoEm": "2026-01-01T00:00:00Z",
				  "payload": {
				    "itens": {
				      "servicos": [{ "servicoId": 1, "descricao": "Troca de óleo", "valor": "120.00" }],
				      "produtos": [{ "produtoId": 7, "descricao": "Filtro de óleo", "quantidade": 1, "precoUnitario": "45.00" }]
				    }
				  }
				}
				""";

		EventoEnvelope<ExecucaoPayloads.DiagnosticoConcluido> envelope =
				mapper.readValue(json, new TypeReference<>() {
				});

		ItensContrato itens = envelope.payload().itens();
		assertFalse(itens.vazio());
		assertEquals("120.00", itens.servicos().get(0).valor());
		assertEquals("45.00", itens.produtos().get(0).precoUnitario());
		assertEquals(1, itens.produtos().get(0).quantidade());
	}

	@Test
	void novoDeveGerarEnvelopeComEventIdEVersao() {
		EventoEnvelope<ExecucaoPayloads.ExecucaoFalhou> envelope = EventoEnvelope.novo(
				TipoEvento.EXECUCAO_FALHOU, 123L, "corr-1",
				new ExecucaoPayloads.ExecucaoFalhou("Peça com defeito"));

		assertNotNull(envelope.eventId());
		assertEquals(EventoEnvelope.VERSAO_ATUAL, envelope.versao());
		assertEquals(123L, envelope.osId());
		assertEquals("corr-1", envelope.correlationId());
		assertNotNull(envelope.ocorridoEm());
	}

	@Test
	void itensVazioDeveDetectarListasVazias() {
		assertTrue(new ItensContrato(java.util.List.of(), java.util.List.of()).vazio());
		assertTrue(new ItensContrato(null, null).vazio());
	}
}
