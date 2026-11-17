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

	private Execucao emDiagnostico() {
		Execucao e = Execucao.criar(123L, 1001L, 42L, 77L, "x", "corr-1", 1);
		e.iniciarDiagnostico(3L);
		return e;
	}

	private Execucao emExecucao() {
		Execucao e = emDiagnostico();
		e.concluirDiagnostico(new Itens(List.of(), List.of()));
		e.iniciarReparo(null);
		return e;
	}

	@Test
	void concluirDiagnosticoDevePublicar() {
		when(repository.buscarPorOsId(123L)).thenReturn(Optional.of(emDiagnostico()));
		when(repository.salvar(any(Execucao.class))).thenAnswer(inv -> inv.getArgument(0));

		Execucao r = service.concluirDiagnostico(123L,
				new Itens(List.of(new Itens.ItemServico(1L, "s", new java.math.BigDecimal("10.00"))), List.of()));

		assertEquals(StatusExecucao.DIAGNOSTICO_CONCLUIDO, r.getStatus());
		verify(publisher).diagnosticoConcluido(r);
	}

	@Test
	void finalizarDevePublicar() {
		when(repository.buscarPorOsId(123L)).thenReturn(Optional.of(emExecucao()));
		when(repository.salvar(any(Execucao.class))).thenAnswer(inv -> inv.getArgument(0));

		Execucao r = service.finalizar(123L);

		assertEquals(StatusExecucao.FINALIZADA, r.getStatus());
		verify(publisher).execucaoFinalizada(r);
	}

	@Test
	void registrarFalhaDevePublicar() {
		when(repository.buscarPorOsId(123L)).thenReturn(Optional.of(emExecucao()));
		when(repository.salvar(any(Execucao.class))).thenAnswer(inv -> inv.getArgument(0));

		Execucao r = service.registrarFalha(123L, "Peça com defeito");

		assertEquals(StatusExecucao.FALHA, r.getStatus());
		assertEquals("Peça com defeito", r.getMotivo());
		verify(publisher).execucaoFalhou(r);
	}

	@Test
	void deveLancarQuandoNaoEncontrada() {
		when(repository.buscarPorOsId(999L)).thenReturn(Optional.empty());
		assertThrows(ExecucaoNaoEncontradaException.class, () -> service.finalizar(999L));
	}
}
