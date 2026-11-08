package br.com.autocenterfiap.execucao.adapter.in.dto;

import br.com.autocenterfiap.execucao.domain.entity.EventoOperacional;
import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;

import java.time.Instant;

public record EventoResponse(
		StatusExecucao statusAnterior,
		StatusExecucao statusNovo,
		String descricao,
		String observacao,
		Instant ocorridoEm) {

	public static EventoResponse de(EventoOperacional evento) {
		return new EventoResponse(
				evento.getStatusAnterior(),
				evento.getStatusNovo(),
				evento.getDescricao(),
				evento.getObservacao(),
				evento.getOcorridoEm());
	}
}
