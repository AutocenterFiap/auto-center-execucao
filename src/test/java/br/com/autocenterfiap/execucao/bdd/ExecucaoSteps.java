package br.com.autocenterfiap.execucao.bdd;

import br.com.autocenterfiap.execucao.adapter.messaging.contract.EventoEnvelope;
import br.com.autocenterfiap.execucao.adapter.messaging.contract.ExecucaoPayloads;
import br.com.autocenterfiap.execucao.adapter.messaging.contract.ItensContrato;
import br.com.autocenterfiap.execucao.adapter.messaging.contract.TipoEvento;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExecucaoSteps {

	private static final Duration TIMEOUT = Duration.ofSeconds(30);

	@Autowired
	private TestRestTemplate rest;
	@Autowired
	private KafkaTemplate<String, Object> kafka;
	@Autowired
	private ExecutionEventCollector collector;
	@Autowired
	private ObjectMapper objectMapper;
	@Value("${execucao.kafka.topics.os-eventos}")
	private String topicoOsEventos;
	@Value("${execucao.kafka.topics.execucao-comandos}")
	private String topicoExecucaoComandos;

	@Before
	public void autenticar() {
		rest.getRestTemplate().setInterceptors(List.of((requisicao, corpo, execucao) -> {
			requisicao.getHeaders().setBearerAuth(TokensTeste.interno());
			return execucao.execute(requisicao, corpo);
		}));
	}

	@Dado("que a OS {long} foi criada")
	public void osCriada(Long osId) {
		var payload = new ExecucaoPayloads.OsCriada(1001L, 42L, 77L, "Barulho ao frear");
		publicar(topicoOsEventos, TipoEvento.OS_CRIADA, osId, payload);
		await().atMost(TIMEOUT).untilAsserted(() ->
				assertEquals(HttpStatus.OK, get(osId).getStatusCode()));
	}

	@Quando("a oficina conclui o diagnostico da OS {long}")
	public void concluiDiagnostico(Long osId) {
		String body = "{\"servicos\":[{\"servicoId\":1,\"descricao\":\"Troca\",\"valor\":120.00}],\"produtos\":[]}";
		ResponseEntity<String> r = rest.postForEntity(
				"/execucoes/" + osId + "/diagnostico", jsonEntity(body), String.class);
		assertEquals(HttpStatus.OK, r.getStatusCode());
	}

	@Quando("a OS {long} manda iniciar o reparo")
	public void iniciarReparo(Long osId) {
		var payload = new ExecucaoPayloads.IniciarReparo(new ItensContrato(List.of(), List.of()));
		publicar(topicoExecucaoComandos, TipoEvento.EXECUCAO_INICIAR_REPARO, osId, payload);
	}

	@Quando("a oficina finaliza a execucao da OS {long}")
	public void finaliza(Long osId) {
		ResponseEntity<String> r = rest.postForEntity(
				"/execucoes/" + osId + "/finalizacao", null, String.class);
		assertEquals(HttpStatus.OK, r.getStatusCode());
	}

	@Entao("a execucao da OS {long} fica com status {string}")
	public void status(Long osId, String statusEsperado) {
		await().atMost(TIMEOUT).untilAsserted(() -> {
			ResponseEntity<String> r = get(osId);
			assertEquals(HttpStatus.OK, r.getStatusCode());
			assertEquals(statusEsperado, objectMapper.readTree(r.getBody()).get("status").asText());
		});
	}

	@Entao("o evento {string} e publicado para a OS {long}")
	public void eventoPublicado(String tipo, Long osId) {
		await().atMost(TIMEOUT).untilAsserted(() ->
				assertTrue(collector.contem(osId, tipo),
						"Evento " + tipo + " nao recebido para a OS " + osId));
	}

	private ResponseEntity<String> get(Long osId) {
		return rest.getForEntity("/execucoes/os/" + osId, String.class);
	}

	private void publicar(String topico, String tipo, Long osId, Object payload) {
		EventoEnvelope<Object> envelope = EventoEnvelope.novo(tipo, osId, "corr-" + osId, payload);
		kafka.send(topico, String.valueOf(osId), envelope);
	}

	private org.springframework.http.HttpEntity<String> jsonEntity(String body) {
		var headers = new org.springframework.http.HttpHeaders();
		headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
		return new org.springframework.http.HttpEntity<>(body, headers);
	}
}
