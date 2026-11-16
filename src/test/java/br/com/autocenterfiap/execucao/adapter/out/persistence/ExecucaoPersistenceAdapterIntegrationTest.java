package br.com.autocenterfiap.execucao.adapter.out.persistence;

import br.com.autocenterfiap.execucao.adapter.out.persistence.repository.ExecucaoMongoRepository;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;
import br.com.autocenterfiap.execucao.domain.entity.Itens;
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

import java.math.BigDecimal;
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
	void salvarEBuscarPorOsIdComItens() {
		Execucao execucao = Execucao.criar(123L, 1001L, 42L, 77L, "Barulho", "corr-1", 1);
		execucao.iniciarDiagnostico(3L);
		execucao.concluirDiagnostico(new Itens(
				List.of(new Itens.ItemServico(1L, "Troca", new BigDecimal("120.00"))), List.of()));

		adapter.salvar(execucao);

		Optional<Execucao> encontrada = adapter.buscarPorOsId(123L);
		assertTrue(encontrada.isPresent());
		assertEquals(StatusExecucao.DIAGNOSTICO_CONCLUIDO, encontrada.get().getStatus());
		assertEquals(3L, encontrada.get().getMecanicoId());
		assertEquals(new BigDecimal("120.00"), encontrada.get().getItens().getServicos().get(0).valor());
	}

	@Test
	void proximaPosicaoFilaSegueAContagem() {
		assertEquals(1, adapter.proximaPosicaoFila());
		adapter.salvar(Execucao.criar(1L, 1L, 1L, 1L, "x", "c", 1));
		assertEquals(2, adapter.proximaPosicaoFila());
	}

	@Test
	void listarFilaTrazSomenteAtivos() {
		adapter.salvar(Execucao.criar(1L, 1L, 1L, 1L, "x", "c", 1));
		Execucao terminal = Execucao.criar(2L, 2L, 1L, 1L, "x", "c", 2);
		terminal.iniciarDiagnostico(1L);
		terminal.concluirDiagnostico(new Itens(List.of(), List.of()));
		terminal.iniciarReparo(null);
		terminal.finalizar();
		adapter.salvar(terminal);

		var fila = adapter.listarFila();

		assertEquals(1, fila.size());
		assertEquals(1L, fila.get(0).getOsId());
		assertFalse(fila.stream().anyMatch(e -> e.getStatus() == StatusExecucao.FINALIZADA));
	}
}
