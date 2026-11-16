package br.com.autocenterfiap.execucao.adapter.out.messaging;

import br.com.autocenterfiap.execucao.adapter.messaging.contract.EventoEnvelope;
import br.com.autocenterfiap.execucao.adapter.messaging.contract.ExecucaoPayloads;
import br.com.autocenterfiap.execucao.adapter.messaging.contract.TipoEvento;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;
import br.com.autocenterfiap.execucao.domain.entity.Itens;
import br.com.autocenterfiap.execucao.infrastructure.config.KafkaTopicsProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExecucaoKafkaPublisherTest {

	@Mock
	KafkaTemplate<String, Object> kafkaTemplate;
	@Captor
	ArgumentCaptor<Object> envelopeCaptor;

	private final KafkaTopicsProperties topics =
			new KafkaTopicsProperties("os.eventos", "execucao.comandos", "execucao.eventos");

	private Execucao comItens() {
		Execucao e = Execucao.criar(123L, 1001L, 42L, 77L, "x", "corr-1", 1);
		e.iniciarDiagnostico(3L);
		e.concluirDiagnostico(new Itens(
				List.of(new Itens.ItemServico(1L, "Troca", new BigDecimal("120.00"))), List.of()));
		return e;
	}

	@Test
	void diagnosticoConcluidoDeveEnvelopeComItensEmStringNoTopicoEComChaveOsId() {
		ExecucaoKafkaPublisher publisher = new ExecucaoKafkaPublisher(kafkaTemplate, topics);

		publisher.diagnosticoConcluido(comItens());

		verify(kafkaTemplate).send(eq("execucao.eventos"), eq("123"), envelopeCaptor.capture());
		@SuppressWarnings("unchecked")
		EventoEnvelope<ExecucaoPayloads.DiagnosticoConcluido> env =
				(EventoEnvelope<ExecucaoPayloads.DiagnosticoConcluido>) envelopeCaptor.getValue();
		assertEquals(TipoEvento.DIAGNOSTICO_CONCLUIDO, env.tipo());
		assertEquals(123L, env.osId());
		assertEquals("corr-1", env.correlationId());
		assertEquals("120.00", env.payload().itens().servicos().get(0).valor());
	}

	@Test
	void execucaoFinalizadaDeveUsarOTopicoEOTipoCorretos() {
		ExecucaoKafkaPublisher publisher = new ExecucaoKafkaPublisher(kafkaTemplate, topics);
		Execucao e = Execucao.criar(55L, 500L, 1L, 1L, "x", "corr-2", 1);

		publisher.execucaoFinalizada(e);

		verify(kafkaTemplate).send(eq("execucao.eventos"), eq("55"), envelopeCaptor.capture());
		@SuppressWarnings("unchecked")
		EventoEnvelope<Object> env = (EventoEnvelope<Object>) envelopeCaptor.getValue();
		assertEquals(TipoEvento.EXECUCAO_FINALIZADA, env.tipo());
	}
}
