package br.com.autocenterfiap.execucao.application.port.in;

import br.com.autocenterfiap.execucao.application.dto.OsCriadaCommand;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;

/**
 * Processa {@code os.criada}: enfileira a execucao e inicia o diagnostico,
 * publicando {@code diagnostico.iniciado}. Idempotente por {@code osId}.
 */
public interface ProcessarOsCriadaUseCase {

	Execucao processar(OsCriadaCommand comando);
}
