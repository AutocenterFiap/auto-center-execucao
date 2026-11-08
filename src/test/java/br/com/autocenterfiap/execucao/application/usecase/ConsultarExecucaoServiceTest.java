package br.com.autocenterfiap.execucao.application.usecase;

import br.com.autocenterfiap.execucao.application.exception.ExecucaoNaoEncontradaException;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoRepositoryPort;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
		return Execucao.criar(123L, 1001L, 42L, 77L, "x", "corr-1", 1);
	}

	@Test
	void buscarPorOsIdEncontrado() {
		when(repository.buscarPorOsId(123L)).thenReturn(Optional.of(umaExecucao()));
		assertEquals(123L, service.buscarPorOsId(123L).getOsId());
	}

	@Test
	void buscarPorOsIdNaoEncontrado() {
		when(repository.buscarPorOsId(999L)).thenReturn(Optional.empty());
		assertThrows(ExecucaoNaoEncontradaException.class, () -> service.buscarPorOsId(999L));
	}

	@Test
	void listarFila() {
		when(repository.listarFila()).thenReturn(List.of(umaExecucao()));
		assertEquals(1, service.listarFila().size());
	}
}
