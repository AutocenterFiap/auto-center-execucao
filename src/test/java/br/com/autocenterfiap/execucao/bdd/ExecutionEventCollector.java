package br.com.autocenterfiap.execucao.bdd;

import br.com.autocenterfiap.execucao.adapter.messaging.contract.EventoEnvelope;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Coleta os eventos publicados em {@code execucao.eventos} para verificacao nos
 * cenarios BDD.
 */
@Component
public class ExecutionEventCollector {

	private final ObjectMapper objectMapper;
	private final List<EventoEnvelope<JsonNode>> eventos = new CopyOnWriteArrayList<>();

	public ExecutionEventCollector(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@KafkaListener(topics = "${execucao.kafka.topics.execucao-eventos}", groupId = "bdd-verifier")
	public void onEvento(String mensagem) throws Exception {
		eventos.add(objectMapper.readValue(mensagem, new TypeReference<>() {
		}));
	}

	public boolean contem(Long osId, String tipo) {
		return eventos.stream()
				.anyMatch(e -> osId.equals(e.osId()) && tipo.equals(e.tipo()));
	}
}
