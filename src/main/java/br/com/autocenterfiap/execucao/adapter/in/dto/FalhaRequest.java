package br.com.autocenterfiap.execucao.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Corpo para registrar a falha de uma execucao, informando o motivo.
 */
@Schema(description = "Registro de falha no reparo")
public record FalhaRequest(
		@Schema(description = "Motivo da falha", example = "Peca com defeito",
				requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank String motivo) {
}
