package br.com.autocenterfiap.execucao.adapter.in.dto;

/**
 * Corpo opcional ao avancar uma etapa (diagnostico, reparo, finalizacao) com uma
 * observacao da oficina.
 */
public record EtapaRequest(String observacao) {
}
