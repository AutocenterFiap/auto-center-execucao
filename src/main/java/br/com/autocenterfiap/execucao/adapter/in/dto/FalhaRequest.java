package br.com.autocenterfiap.execucao.adapter.in.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Corpo para registrar a falha de uma execucao, informando o motivo.
 */
public record FalhaRequest(@NotBlank String motivo) {
}
