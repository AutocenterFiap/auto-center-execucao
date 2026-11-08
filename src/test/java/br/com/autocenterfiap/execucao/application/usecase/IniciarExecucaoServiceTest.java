package br.com.autocenterfiap.execucao.application.usecase;

import br.com.autocenterfiap.execucao.application.dto.IniciarExecucaoCommand;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoEventPublisherPort;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IniciarExecucaoServiceTest {

	@Mock
	ExecucaoRepositoryPort repository;
	@Mock
	ExecucaoEventPublisherPort publisher;
	@InjectMocks
	IniciarExecucaoService service;

	@Test
	void deveCriarEnfileirarEPublicarQuandoNaoExiste() {
		when(repository.buscarPorOsId("os-1")).thenReturn(Optional.empty());
		when(repository.proximaPosicaoFila()).thenReturn(5L);
		when(repository.salvar(any(Execucao.class))).thenAnswer(inv -> inv.getArgument(0));

		Execucao resultado = service.iniciar(new IniciarExecucaoCommand("os-1"));

		assertEquals(StatusExecucao.NA_FILA, resultado.getStatus());
		assertEquals("os-1", resultado.getOsId());
		assertEquals(5, resultado.getPosicaoFila());
		verify(repository).salvar(any(Execucao.class));
		verify(publisher).publicarExecucaoIniciada(resultado);
	}

	@Test
	void deveSerIdempotenteQuandoJaExiste() {
		Execucao existente = Execucao.reconstituir("exec-1", "os-1", StatusExecucao.EM_EXECUCAO, 1,
				null, Instant.now(), Instant.now(), null, new ArrayList<>());
		when(repository.buscarPorOsId("os-1")).thenReturn(Optional.of(existente));

		Execucao resultado = service.iniciar(new IniciarExecucaoCommand("os-1"));

		assertSame(existente, resultado);
		verify(repository, never()).salvar(any());
		verify(publisher, never()).publicarExecucaoIniciada(any());
	}
}
