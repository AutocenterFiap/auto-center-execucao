package br.com.autocenterfiap.execucao.infrastructure.config;

import br.com.autocenterfiap.execucao.application.port.in.ConsultarExecucaoUseCase;
import br.com.autocenterfiap.execucao.application.port.in.GerenciarExecucaoUseCase;
import br.com.autocenterfiap.execucao.application.port.in.IniciarExecucaoUseCase;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoEventPublisherPort;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoRepositoryPort;
import br.com.autocenterfiap.execucao.application.usecase.ConsultarExecucaoService;
import br.com.autocenterfiap.execucao.application.usecase.GerenciarExecucaoService;
import br.com.autocenterfiap.execucao.application.usecase.IniciarExecucaoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Monta os casos de uso (camada de aplicacao, livre de framework) injetando as
 * implementacoes das ports de saida fornecidas pela infraestrutura.
 */
@Configuration
public class UseCaseConfig {

	@Bean
	public IniciarExecucaoUseCase iniciarExecucaoUseCase(ExecucaoRepositoryPort repository,
	                                                     ExecucaoEventPublisherPort publisher) {
		return new IniciarExecucaoService(repository, publisher);
	}

	@Bean
	public GerenciarExecucaoUseCase gerenciarExecucaoUseCase(ExecucaoRepositoryPort repository,
	                                                         ExecucaoEventPublisherPort publisher) {
		return new GerenciarExecucaoService(repository, publisher);
	}

	@Bean
	public ConsultarExecucaoUseCase consultarExecucaoUseCase(ExecucaoRepositoryPort repository) {
		return new ConsultarExecucaoService(repository);
	}
}
