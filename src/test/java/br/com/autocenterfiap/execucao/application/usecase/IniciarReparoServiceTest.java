package br.com.autocenterfiap.execucao.application.usecase;

import br.com.autocenterfiap.execucao.application.exception.ExecucaoNaoEncontradaException;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoEventPublisherPort;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoRepositoryPort;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;
import br.com.autocenterfiap.execucao.domain.entity.Itens;
import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IniciarReparoServiceTest {

	@Mock
	ExecucaoRepositoryPort repository;
	@Mock
	ExecucaoEventPublisherPort publisher;
	@InjectMocks
	IniciarReparoService service;

	private Execucao emDiagnosticoConcluido() {
		Execucao e = Execucao.criar(123L, 1001L, 42L, 77L, "x", "corr-1", 1);
		e.iniciarDiagnostico(3L);
		e.concluirDiagnostico(new Itens(List.of(), List.of()));
		return e;
	}

	@Test
	void deveIniciarReparoEPublicar() {
		when(repository.buscarPorOsId(123L)).thenReturn(Optional.of(emDiagnosticoConcluido()));
		when(repository.salvar(any(Execucao.class))).thenAnswer(inv -> inv.getArgument(0));

		Execucao resultado = service.iniciar(123L);

		assertEquals(StatusExecucao.EM_EXECUCAO, resultado.getStatus());
		verify(publisher).execucaoIniciada(resultado);
	}

	@Test
	void deveLancarQuandoNaoEncontrada() {
		when(repository.buscarPorOsId(999L)).thenReturn(Optional.empty());
		assertThrows(ExecucaoNaoEncontradaException.class, () -> service.iniciar(999L));
		verify(repository, never()).salvar(any());
	}
}
