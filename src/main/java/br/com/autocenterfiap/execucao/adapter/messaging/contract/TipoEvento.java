package br.com.autocenterfiap.execucao.adapter.messaging.contract;

/**
 * Valores do campo {@code tipo} do envelope, conforme o contrato da Saga.
 */
public final class TipoEvento {

	private TipoEvento() {
	}

	// ─── Consumidos pela Execucao ────────────────────────────────────
	public static final String OS_CRIADA = "os.criada";
	public static final String OS_CANCELADA = "os.cancelada";
	public static final String EXECUCAO_INICIAR_REPARO = "execucao.iniciar-reparo";

	// ─── Publicados pela Execucao ────────────────────────────────────
	public static final String DIAGNOSTICO_INICIADO = "diagnostico.iniciado";
	public static final String DIAGNOSTICO_CONCLUIDO = "diagnostico.concluido";
	public static final String EXECUCAO_INICIADA = "execucao.iniciada";
	public static final String EXECUCAO_FINALIZADA = "execucao.finalizada";
	public static final String EXECUCAO_FALHOU = "execucao.falhou";
}
