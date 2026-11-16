package br.com.autocenterfiap.execucao.adapter.in.consumer;

import br.com.autocenterfiap.execucao.application.dto.OsCriadaCommand;
import br.com.autocenterfiap.execucao.application.port.in.CancelarExecucaoUseCase;
import br.com.autocenterfiap.execucao.application.port.in.ProcessarOsCriadaUseCase;
import br.com.autocenterfiap.execucao.application.port.out.EventoProcessadoPort;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OsEventosConsumerTest {

	@Mock
	ProcessarOsCriadaUseCase processarOsCriada;
	@Mock
	CancelarExecucaoUseCase cancelarExecucao;
	@Mock
	EventoProcessadoPort eventoProcessado;
	@Captor
	ArgumentCaptor<OsCriadaCommand> comandoCaptor;

	private final ObjectMapper mapper = JsonMapper.builder()
			.addModule(new JavaTimeModule())
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.build();

	private OsEventosConsumer consumer() {
		return new OsEventosConsumer(processarOsCriada, cancelarExecucao, eventoProcessado, mapper);
	}

	@Test
	void deveRotearOsCriadaEMarcarProcessado() throws Exception {
		when(eventoProcessado.jaProcessado("e1")).thenReturn(false);
		String json = """
				{"eventId":"e1","tipo":"os.criada","versao":1,"osId":123,
				 "correlationId":"c1","ocorridoEm":"2026-01-01T00:00:00Z",
				 "payload":{"numero":1001,"clienteId":42,"veiculoId":77,"problemaRelatado":"Barulho"}}
				""";

		consumer().onMensagem(json);

		verify(processarOsCriada).processar(comandoCaptor.capture());
		assertEquals(123L, comandoCaptor.getValue().osId());
		assertEquals(1001L, comandoCaptor.getValue().numero());
		verify(eventoProcessado).marcarProcessado("e1");
	}

	@Test
	void deveRotearOsCancelada() throws Exception {
		when(eventoProcessado.jaProcessado("e2")).thenReturn(false);
		String json = """
				{"eventId":"e2","tipo":"os.cancelada","versao":1,"osId":123,
				 "correlationId":"c1","ocorridoEm":"2026-01-01T00:00:00Z",
				 "payload":{"motivo":"ORCAMENTO_REPROVADO"}}
				""";

		consumer().onMensagem(json);

		verify(cancelarExecucao).cancelar(123L, "ORCAMENTO_REPROVADO");
		verify(eventoProcessado).marcarProcessado("e2");
	}

	@Test
	void deveDescartarEventoJaProcessado() throws Exception {
		when(eventoProcessado.jaProcessado("e1")).thenReturn(true);
		String json = """
				{"eventId":"e1","tipo":"os.criada","versao":1,"osId":123,
				 "correlationId":"c1","ocorridoEm":"2026-01-01T00:00:00Z","payload":{}}
				""";

		consumer().onMensagem(json);

		verifyNoInteractions(processarOsCriada, cancelarExecucao);
		verify(eventoProcessado, never()).marcarProcessado("e1");
	}
}
