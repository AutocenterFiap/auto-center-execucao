package br.com.autocenterfiap.execucao.application.usecase;

import br.com.autocenterfiap.execucao.application.exception.ExecucaoNaoEncontradaException;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoRepositoryPort;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;
import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarExecucaoServiceTest {

	@Mock
	ExecucaoRepositoryPort repository;
	@InjectMocks
	ConsultarExecucaoService service;

	private Execucao umaExecucao() {
		return Execucao.reconstituir("exec-1", "os-1", StatusExecucao.NA_FILA, 1, null,
				Instant.now(), Instant.now(), null, new ArrayList<>());
	}

	@Test
	void buscarPorIdEncontrado() {
		when(repository.buscarPorId("exec-1")).thenReturn(Optional.of(umaExecucao()));
		assertEquals("exec-1", service.buscarPorId("exec-1").getId());
	}

	@Test
	void buscarPorIdNaoEncontrado() {
		when(repository.buscarPorId("x")).thenReturn(Optional.empty());
		assertThrows(ExecucaoNaoEncontradaException.class, () -> service.buscarPorId("x"));
	}

	@Test
	void buscarPorOsIdNaoEncontrado() {
		when(repository.buscarPorOsId("os-x")).thenReturn(Optional.empty());
		assertThrows(ExecucaoNaoEncontradaException.class, () -> service.buscarPorOsId("os-x"));
	}

	@Test
	void listarFila() {
		when(repository.listarFila()).thenReturn(List.of(umaExecucao()));
		assertEquals(1, service.listarFila().size());
	}
}
