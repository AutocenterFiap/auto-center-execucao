package br.com.autocenterfiap.execucao.adapter.in.consumer;

import br.com.autocenterfiap.execucao.application.dto.IniciarExecucaoCommand;
import br.com.autocenterfiap.execucao.application.port.in.IniciarExecucaoUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PagamentoConfirmadoConsumerTest {

	@Mock
	IniciarExecucaoUseCase useCase;
	@Captor
	ArgumentCaptor<IniciarExecucaoCommand> comandoCaptor;

	@Test
	void deveIniciarExecucaoComOsIdDoEvento() {
		PagamentoConfirmadoConsumer consumer = new PagamentoConfirmadoConsumer(useCase);

		consumer.onPagamentoConfirmado(new PagamentoConfirmadoEvent("os-123"));

		verify(useCase).iniciar(comandoCaptor.capture());
		assertEquals("os-123", comandoCaptor.getValue().osId());
	}
}
