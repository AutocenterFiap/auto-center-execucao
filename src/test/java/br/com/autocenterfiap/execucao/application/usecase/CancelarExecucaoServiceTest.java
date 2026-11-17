package br.com.autocenterfiap.execucao.application.usecase;

import br.com.autocenterfiap.execucao.application.port.out.ExecucaoRepositoryPort;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;
import br.com.autocenterfiap.execucao.domain.entity.Itens;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelarExecucaoServiceTest {

	@Mock
	ExecucaoRepositoryPort repository;
	@InjectMocks
	CancelarExecucaoService service;

	@Test
	void deveCancelarExecucaoAtivaESalvar() {
		Execucao ativa = Execucao.criar(123L, 1001L, 42L, 77L, "x", "corr-1", 1);
		ativa.iniciarDiagnostico(3L);
		when(repository.buscarPorOsId(123L)).thenReturn(Optional.of(ativa));

		service.cancelar(123L, "ORCAMENTO_REPROVADO");

		verify(repository).salvar(ativa);
	}

	@Test
	void naoDeveSalvarQuandoJaTerminal() {
		Execucao finalizada = Execucao.criar(123L, 1001L, 42L, 77L, "x", "corr-1", 1);
		finalizada.iniciarDiagnostico(3L);
		finalizada.concluirDiagnostico(new Itens(List.of(), List.of()));
		finalizada.iniciarReparo(null);
		finalizada.finalizar();
		when(repository.buscarPorOsId(123L)).thenReturn(Optional.of(finalizada));

		service.cancelar(123L, "tarde demais");

		verify(repository, never()).salvar(any());
	}

	@Test
	void naoDeveFazerNadaQuandoNaoExiste() {
		when(repository.buscarPorOsId(999L)).thenReturn(Optional.empty());

		service.cancelar(999L, "qualquer");

		verify(repository, never()).salvar(any());
	}
}
