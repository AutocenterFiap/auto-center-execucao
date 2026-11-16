package br.com.autocenterfiap.execucao.adapter.in.consumer;

import br.com.autocenterfiap.execucao.application.port.in.IniciarReparoUseCase;
import br.com.autocenterfiap.execucao.application.port.out.EventoProcessadoPort;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecucaoComandosConsumerTest {

	@Mock
	IniciarReparoUseCase iniciarReparo;
	@Mock
	EventoProcessadoPort eventoProcessado;

	private final ObjectMapper mapper = JsonMapper.builder()
			.addModule(new JavaTimeModule())
			.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			.build();

	@Test
	void deveIniciarReparoAoReceberOComando() throws Exception {
		when(eventoProcessado.jaProcessado("e9")).thenReturn(false);
		String json = """
				{"eventId":"e9","tipo":"execucao.iniciar-reparo","versao":1,"osId":123,
				 "correlationId":"c1","ocorridoEm":"2026-01-01T00:00:00Z",
				 "payload":{"itens":{"servicos":[],"produtos":[]}}}
				""";

		new ExecucaoComandosConsumer(iniciarReparo, eventoProcessado, mapper).onMensagem(json);

		verify(iniciarReparo).iniciar(123L);
		verify(eventoProcessado).marcarProcessado("e9");
	}
}
