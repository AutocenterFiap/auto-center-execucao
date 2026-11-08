package br.com.autocenterfiap.execucao.adapter.out.persistence;

import br.com.autocenterfiap.execucao.domain.entity.Execucao;
import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import br.com.autocenterfiap.execucao.adapter.out.persistence.repository.ExecucaoMongoRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@DataMongoTest
class ExecucaoPersistenceAdapterIntegrationTest {

	@Container
	static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7");

	@DynamicPropertySource
	static void mongoProps(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
	}

	@Autowired
	ExecucaoMongoRepository repository;

	ExecucaoPersistenceAdapter adapter;

	@BeforeEach
	void setUp() {
		repository.deleteAll();
		adapter = new ExecucaoPersistenceAdapter(repository);
	}

	@Test
	void salvarEBuscarPorIdEOsId() {
		Execucao salva = adapter.salvar(Execucao.criarNaFila("os-1", 1));

		assertEquals("os-1", salva.getOsId());
		Optional<Execucao> porId = adapter.buscarPorId(salva.getId());
		Optional<Execucao> porOs = adapter.buscarPorOsId("os-1");
		assertTrue(porId.isPresent());
		assertTrue(porOs.isPresent());
		assertEquals(salva.getId(), porOs.get().getId());
		assertEquals(1, porId.get().getHistorico().size());
	}

	@Test
	void proximaPosicaoFilaDeveSeguirAContagem() {
		assertEquals(1, adapter.proximaPosicaoFila());
		adapter.salvar(Execucao.criarNaFila("os-1", 1));
		assertEquals(2, adapter.proximaPosicaoFila());
	}

	@Test
	void listarFilaDeveTrazerSomenteAtivosOrdenados() {
		adapter.salvar(Execucao.criarNaFila("os-1", 1));
		Execucao segunda = Execucao.criarNaFila("os-2", 2);
		segunda.iniciarDiagnostico(null);
		adapter.salvar(segunda);
		Execucao terceira = Execucao.criarNaFila("os-3", 3);
		terceira.iniciarDiagnostico(null);
		terceira.iniciarReparo(null);
		terceira.finalizar(null); // terminal: nao deve aparecer na fila
		adapter.salvar(terceira);

		List<Execucao> fila = adapter.listarFila();

		assertEquals(2, fila.size());
		assertEquals("os-1", fila.get(0).getOsId());
		assertEquals("os-2", fila.get(1).getOsId());
		assertFalse(fila.stream().anyMatch(e -> e.getStatus() == StatusExecucao.FINALIZADA));
	}
}
