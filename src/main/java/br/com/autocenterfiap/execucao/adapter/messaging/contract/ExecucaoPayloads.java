package br.com.autocenterfiap.execucao.adapter.messaging.contract;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Payloads das mensagens que a Execucao consome e publica, conforme o contrato.
 */
public final class ExecucaoPayloads {

	private ExecucaoPayloads() {
	}

	// ─── Consumidos ──────────────────────────────────────────────────

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record OsCriada(Long numero, Long clienteId, Long veiculoId, String problemaRelatado) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record OsCancelada(String motivo) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record IniciarReparo(ItensContrato itens) {
	}

	// ─── Publicados ──────────────────────────────────────────────────

	public record DiagnosticoIniciado(Long mecanicoId) {
	}

	public record DiagnosticoConcluido(ItensContrato itens) {
	}

	public record ExecucaoIniciada(Long mecanicoId) {
	}

	public record ExecucaoFinalizada() {
	}

	public record ExecucaoFalhou(String motivo) {
	}
}
