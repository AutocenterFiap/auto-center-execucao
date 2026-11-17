package br.com.autocenterfiap.execucao.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	private static final String ESQUEMA = "bearerAuth";

	@Bean
	OpenAPI openApi() {
		return new OpenAPI()
				.info(new Info().title("AutoCenter - Execução").version("1.0.0")
						.description("Fila da oficina, diagnóstico e reparo das ordens de serviço. Participa da "
								+ "Saga consumindo comandos e publicando eventos no Kafka e consulta o catálogo "
								+ "no serviço de Cadastro."))
				.components(new Components().addSecuritySchemes(ESQUEMA,
						new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
								.description("JWT de funcionário, com o perfil interno")))
				.addSecurityItem(new SecurityRequirement().addList(ESQUEMA));
	}
}
