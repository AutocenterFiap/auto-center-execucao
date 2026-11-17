package br.com.autocenterfiap.execucao.application.dto;

/**
 * Dados do evento {@code os.criada} para colocar a OS na fila e iniciar o diagnostico.
 */
public record OsCriadaCommand(
		Long osId,
		Long numero,
		Long clienteId,
		Long veiculoId,
		String problemaRelatado,
		String correlationId) {
}
