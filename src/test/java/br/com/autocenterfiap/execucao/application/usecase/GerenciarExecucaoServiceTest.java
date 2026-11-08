package br.com.autocenterfiap.execucao.application.usecase;

import br.com.autocenterfiap.execucao.application.exception.ExecucaoNaoEncontradaException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GerenciarExecucaoServiceTest {

	@Mock
	ExecucaoRepositoryPort repository;
	@Mock
	ExecucaoEventPublisherPort publisher;
	@InjectMocks
	GerenciarExecucaoService service;

	private Execucao execucaoEm(StatusExecucao status) {
		return Execucao.reconstituir("exec-1", "os-1", status, 1, null,
				Instant.now(), Instant.now(), null, new ArrayList<>());
	}

	@Test
	void iniciarDiagnosticoDeveTransitarESalvar() {
		when(repository.buscarPorId("exec-1")).thenReturn(Optional.of(execucaoEm(StatusExecucao.NA_FILA)));
		when(repository.salvar(any(Execucao.class))).thenAnswer(inv -> inv.getArgument(0));

		Execucao resultado = service.iniciarDiagnostico("exec-1", "ok");

		assertEquals(StatusExecucao.EM_DIAGNOSTICO, resultado.getStatus());
		verify(repository).salvar(resultado);
		verify(publisher, never()).publicarExecucaoFinalizada(any());
	}

	@Test
	void finalizarDevePublicarEventoFinalizada() {
		when(repository.buscarPorId("exec-1")).thenReturn(Optional.of(execucaoEm(StatusExecucao.EM_EXECUCAO)));
		when(repository.salvar(any(Execucao.class))).thenAnswer(inv -> inv.getArgument(0));

		Execucao resultado = service.finalizar("exec-1", "concluido");

		assertEquals(StatusExecucao.FINALIZADA, resultado.getStatus());
		verify(publisher).publicarExecucaoFinalizada(resultado);
	}

	@Test
	void registrarFalhaDevePublicarEventoFalhou() {
		when(repository.buscarPorId("exec-1")).thenReturn(Optional.of(execucaoEm(StatusExecucao.EM_DIAGNOSTICO)));
		when(repository.salvar(any(Execucao.class))).thenAnswer(inv -> inv.getArgument(0));

		Execucao resultado = service.registrarFalha("exec-1", "peca indisponivel");

		assertEquals(StatusExecucao.FALHA, resultado.getStatus());
		assertEquals("peca indisponivel", resultado.getMotivoFalha());
		verify(publisher).publicarExecucaoFalhou(resultado);
	}

	@Test
	void deveLancarQuandoExecucaoNaoEncontrada() {
		when(repository.buscarPorId("inexistente")).thenReturn(Optional.empty());

		assertThrows(ExecucaoNaoEncontradaException.class,
				() -> service.iniciarReparo("inexistente", null));
		verify(repository, never()).salvar(any());
	}
}
