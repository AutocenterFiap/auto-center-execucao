package br.com.autocenterfiap.execucao.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Configuracao do contexto Spring para os cenarios BDD, com MongoDB e Kafka reais
 * (Testcontainers). Sobe a aplicacao completa em uma porta aleatoria.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class CucumberSpringConfiguration {

	static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7");
	static final KafkaContainer KAFKA =
			new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.1"));

	static {
		MONGO.start();
		KAFKA.start();
	}

	@DynamicPropertySource
	static void props(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
		registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
		registry.add("sistema.seguranca.chave.secreta", () -> TokensTeste.SEGREDO);
	}
}
