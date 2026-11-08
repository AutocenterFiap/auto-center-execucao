package br.com.autocenterfiap.execucao.adapter.out.messaging;

import br.com.autocenterfiap.execucao.domain.entity.Execucao;
import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;
import br.com.autocenterfiap.execucao.infrastructure.config.KafkaTopicsProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExecucaoKafkaPublisherTest {

	@Mock
	KafkaTemplate<String, Object> kafkaTemplate;
	@Captor
	ArgumentCaptor<Object> payloadCaptor;

	private final KafkaTopicsProperties topics = new KafkaTopicsProperties(
			"pagamento.confirmado", "execucao.iniciada", "execucao.finalizada", "execucao.falhou");

	private Execucao execucao(StatusExecucao status) {
		return Execucao.reconstituir("exec-1", "os-99", status, 1, null,
				Instant.now(), Instant.now(), null, new ArrayList<>());
	}

	@Test
	void publicarFinalizadaDeveEnviarNoTopicoComChaveOsId() {
		ExecucaoKafkaPublisher publisher = new ExecucaoKafkaPublisher(kafkaTemplate, topics);

		publisher.publicarExecucaoFinalizada(execucao(StatusExecucao.FINALIZADA));

		verify(kafkaTemplate).send(org.mockito.ArgumentMatchers.eq("execucao.finalizada"),
				org.mockito.ArgumentMatchers.eq("os-99"), payloadCaptor.capture());
		ExecucaoEventPayload payload = (ExecucaoEventPayload) payloadCaptor.getValue();
		assertEquals("execucao.finalizada", payload.evento());
		assertEquals("os-99", payload.osId());
		assertEquals(StatusExecucao.FINALIZADA, payload.status());
	}

	@Test
	void publicarIniciadaEFalhouUsamOsTopicosCorretos() {
		ExecucaoKafkaPublisher publisher = new ExecucaoKafkaPublisher(kafkaTemplate, topics);

		publisher.publicarExecucaoIniciada(execucao(StatusExecucao.NA_FILA));
		publisher.publicarExecucaoFalhou(execucao(StatusExecucao.FALHA));

		verify(kafkaTemplate).send(org.mockito.ArgumentMatchers.eq("execucao.iniciada"),
				org.mockito.ArgumentMatchers.eq("os-99"), org.mockito.ArgumentMatchers.any());
		verify(kafkaTemplate).send(org.mockito.ArgumentMatchers.eq("execucao.falhou"),
				org.mockito.ArgumentMatchers.eq("os-99"), org.mockito.ArgumentMatchers.any());
	}
}
