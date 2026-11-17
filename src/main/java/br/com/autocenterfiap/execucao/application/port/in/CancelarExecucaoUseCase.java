package br.com.autocenterfiap.execucao.application.port.in;

/**
 * Processa {@code os.cancelada}: cancela a execucao (compensacao). Tolerante: se a
 * execucao nao existir ou ja estiver terminal, nada acontece.
 */
public interface CancelarExecucaoUseCase {

	void cancelar(Long osId, String motivo);
}
