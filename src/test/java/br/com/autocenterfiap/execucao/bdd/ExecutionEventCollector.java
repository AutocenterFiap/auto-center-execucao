package br.com.autocenterfiap.execucao.bdd;

import br.com.autocenterfiap.execucao.adapter.out.messaging.ExecucaoEventPayload;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Componente de teste que coleta os eventos de execucao publicados no Kafka para
 * verificacao nos cenarios BDD.
 */
@Component
public class ExecutionEventCollector {

	private final List<ExecucaoEventPayload> eventos = new CopyOnWriteArrayList<>();

	@KafkaListener(topics = "${execucao.kafka.topics.execucao-finalizada}", groupId = "bdd-verifier")
	public void onExecucaoFinalizada(ExecucaoEventPayload payload) {
		eventos.add(payload);
	}

	public boolean contem(String osId, String evento) {
		return eventos.stream()
				.anyMatch(e -> osId.equals(e.osId()) && evento.equals(e.evento()));
	}
}
