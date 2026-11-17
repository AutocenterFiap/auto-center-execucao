package br.com.autocenterfiap.execucao.application.usecase;

import br.com.autocenterfiap.execucao.application.dto.OsCriadaCommand;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoEventPublisherPort;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoRepositoryPort;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;
import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessarOsCriadaServiceTest {

	@Mock
	ExecucaoRepositoryPort repository;
	@Mock
	ExecucaoEventPublisherPort publisher;
	@InjectMocks
	ProcessarOsCriadaService service;

	private OsCriadaCommand comando() {
		return new OsCriadaCommand(123L, 1001L, 42L, 77L, "Barulho ao frear", "corr-1");
	}

	@Test
	void deveCriarEnfileirarIniciarDiagnosticoEPublicar() {
		when(repository.buscarPorOsId(123L)).thenReturn(Optional.empty());
		when(repository.proximaPosicaoFila()).thenReturn(3L);
		when(repository.salvar(any(Execucao.class))).thenAnswer(inv -> inv.getArgument(0));

		Execucao resultado = service.processar(comando());

		assertEquals(StatusExecucao.EM_DIAGNOSTICO, resultado.getStatus());
		assertEquals(3, resultado.getPosicaoFila());
		assertEquals(3L, resultado.getMecanicoId()); // floorMod(122,5)+1
		verify(publisher).diagnosticoIniciado(resultado);
	}

	@Test
	void deveSerIdempotenteQuandoJaExiste() {
		Execucao existente = Execucao.criar(123L, 1001L, 42L, 77L, "x", "corr-1", 1);
		when(repository.buscarPorOsId(123L)).thenReturn(Optional.of(existente));

		Execucao resultado = service.processar(comando());

		assertSame(existente, resultado);
		verify(repository, never()).salvar(any());
		verify(publisher, never()).diagnosticoIniciado(any());
	}
}
