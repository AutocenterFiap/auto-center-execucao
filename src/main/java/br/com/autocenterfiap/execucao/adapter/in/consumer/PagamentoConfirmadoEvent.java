package br.com.autocenterfiap.execucao.adapter.in.consumer;

/**
 * Evento recebido do serviço de Orçamento e Pagamento quando o pagamento de uma OS
 * e confirmado. Dispara a entrada da OS na fila de execucao.
 */
public record PagamentoConfirmadoEvent(String osId) {
}
