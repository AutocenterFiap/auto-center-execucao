package br.com.autocenterfiap.execucao.adapter.in.dto;

import br.com.autocenterfiap.execucao.domain.entity.EventoOperacional;
import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Evento operacional registrado no histórico da execução")
public record EventoResponse(
		@Schema(description = "Status antes do evento; nulo na entrada na fila", example = "EM_DIAGNOSTICO")
		StatusExecucao statusAnterior,
		@Schema(description = "Status após o evento", example = "DIAGNOSTICO_CONCLUIDO") StatusExecucao statusNovo,
		@Schema(description = "Descrição do evento", example = "Diagnostico concluido") String descricao,
		@Schema(description = "Observação complementar, como o motivo de uma falha", example = "Peca com defeito")
		String observacao,
		@Schema(description = "Momento do evento", example = "2026-11-17T12:30:00Z") Instant ocorridoEm) {

	public static EventoResponse de(EventoOperacional evento) {
		return new EventoResponse(
				evento.getStatusAnterior(),
				evento.getStatusNovo(),
				evento.getDescricao(),
				evento.getObservacao(),
				evento.getOcorridoEm());
	}
}
