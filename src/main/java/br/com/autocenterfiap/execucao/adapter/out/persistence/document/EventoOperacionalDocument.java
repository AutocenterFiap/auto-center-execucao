package br.com.autocenterfiap.execucao.adapter.out.persistence.document;

import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Representacao persistente (embutida) de um evento operacional do historico.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoOperacionalDocument {

	private StatusExecucao statusAnterior;
	private StatusExecucao statusNovo;
	private String descricao;
	private String observacao;
	private Instant ocorridoEm;
}
