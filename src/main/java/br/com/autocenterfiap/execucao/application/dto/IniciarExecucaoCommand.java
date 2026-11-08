package br.com.autocenterfiap.execucao.application.dto;

/**
 * Dados necessarios para colocar uma OS na fila de execucao. Originado do evento
 * {@code pagamento.confirmado}.
 */
public record IniciarExecucaoCommand(String osId) {
}
