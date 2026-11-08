package br.com.autocenterfiap.execucao.bdd;

import br.com.autocenterfiap.execucao.adapter.in.consumer.PagamentoConfirmadoEvent;
import br.com.autocenterfiap.execucao.adapter.in.dto.EtapaRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
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
	@Value("${execucao.kafka.topics.pagamento-confirmado}")
	private String topicoPagamentoConfirmado;

	private String osId;
	private String execucaoId;

	@Dado("que o pagamento da OS {string} foi confirmado")
	public void pagamentoConfirmado(String osId) {
		this.osId = osId;
		kafka.send(topicoPagamentoConfirmado, osId, new PagamentoConfirmadoEvent(osId));

		await().atMost(TIMEOUT).untilAsserted(() -> {
			ResponseEntity<String> resposta =
					rest.getForEntity("/execucoes/os/" + osId, String.class);
			assertEquals(HttpStatus.OK, resposta.getStatusCode());
			execucaoId = objectMapper.readTree(resposta.getBody()).get("id").asText();
		});
	}

	@Quando("a oficina inicia o diagnostico da execucao")
	public void iniciaDiagnostico() {
		avancarEtapa("diagnostico");
	}

	@E("a oficina inicia o reparo")
	public void iniciaReparo() {
		avancarEtapa("reparo");
	}

	@E("a oficina finaliza a execucao")
	public void finaliza() {
		avancarEtapa("finalizacao");
	}

	@Entao("a execucao da OS {string} fica com status {string}")
	public void statusDaExecucao(String osId, String statusEsperado) throws Exception {
		ResponseEntity<String> resposta =
				rest.getForEntity("/execucoes/os/" + osId, String.class);
		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		String status = objectMapper.readTree(resposta.getBody()).get("status").asText();
		assertEquals(statusEsperado, status);
	}

	@E("o evento {string} e publicado para a OS {string}")
	public void eventoPublicado(String evento, String osId) {
		await().atMost(TIMEOUT)
				.untilAsserted(() -> assertTrue(collector.contem(osId, evento),
						"Evento " + evento + " nao recebido para a OS " + osId));
	}

	private void avancarEtapa(String etapa) {
		ResponseEntity<String> resposta = rest.postForEntity(
				"/execucoes/" + execucaoId + "/" + etapa,
				new EtapaRequest("via BDD"),
				String.class);
		assertEquals(HttpStatus.OK, resposta.getStatusCode());
	}
}
