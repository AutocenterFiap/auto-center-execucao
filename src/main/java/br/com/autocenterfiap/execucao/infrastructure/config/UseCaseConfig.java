package br.com.autocenterfiap.execucao.infrastructure.config;

import br.com.autocenterfiap.execucao.application.port.in.CancelarExecucaoUseCase;
import br.com.autocenterfiap.execucao.application.port.in.ConsultarExecucaoUseCase;
import br.com.autocenterfiap.execucao.application.port.in.GerenciarExecucaoUseCase;
import br.com.autocenterfiap.execucao.application.port.in.IniciarReparoUseCase;
import br.com.autocenterfiap.execucao.application.port.in.ProcessarOsCriadaUseCase;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoEventPublisherPort;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoRepositoryPort;
import br.com.autocenterfiap.execucao.application.usecase.CancelarExecucaoService;
import br.com.autocenterfiap.execucao.application.usecase.ConsultarExecucaoService;
import br.com.autocenterfiap.execucao.application.usecase.GerenciarExecucaoService;
import br.com.autocenterfiap.execucao.application.usecase.IniciarReparoService;
import br.com.autocenterfiap.execucao.application.usecase.ProcessarOsCriadaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Monta os casos de uso (camada de aplicacao, livre de framework) a partir das ports.
 */
@Configuration
public class UseCaseConfig {

	@Bean
	public ProcessarOsCriadaUseCase processarOsCriadaUseCase(ExecucaoRepositoryPort repository,
	                                                         ExecucaoEventPublisherPort publisher) {
		return new ProcessarOsCriadaService(repository, publisher);
	}

	@Bean
	public IniciarReparoUseCase iniciarReparoUseCase(ExecucaoRepositoryPort repository,
	                                                 ExecucaoEventPublisherPort publisher) {
		return new IniciarReparoService(repository, publisher);
	}

	@Bean
	public GerenciarExecucaoUseCase gerenciarExecucaoUseCase(ExecucaoRepositoryPort repository,
	                                                         ExecucaoEventPublisherPort publisher) {
		return new GerenciarExecucaoService(repository, publisher);
	}

	@Bean
	public CancelarExecucaoUseCase cancelarExecucaoUseCase(ExecucaoRepositoryPort repository) {
		return new CancelarExecucaoService(repository);
	}

	@Bean
	public ConsultarExecucaoUseCase consultarExecucaoUseCase(ExecucaoRepositoryPort repository) {
		return new ConsultarExecucaoService(repository);
	}
}
