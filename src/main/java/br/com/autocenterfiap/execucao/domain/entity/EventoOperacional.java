package br.com.autocenterfiap.execucao.domain.entity;

import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Registro imutavel de uma etapa/evento operacional ocorrido no ciclo de vida de
 * uma {@link Execucao}. O conjunto desses registros forma o historico guardado no
 * banco NoSQL.
 */
@Getter
public class EventoOperacional {

	private final StatusExecucao statusAnterior;
	private final StatusExecucao statusNovo;
	private final String descricao;
	private final String observacao;
	private final Instant ocorridoEm;

	public EventoOperacional(StatusExecucao statusAnterior, StatusExecucao statusNovo,
	                         String descricao, String observacao, Instant ocorridoEm) {
		this.statusAnterior = statusAnterior;
		this.statusNovo = Objects.requireNonNull(statusNovo, "statusNovo e obrigatorio");
		this.descricao = descricao;
		this.observacao = observacao;
		this.ocorridoEm = Objects.requireNonNull(ocorridoEm, "ocorridoEm e obrigatorio");
	}

	/**
	 * Cria um evento marcando o instante atual.
	 */
	public static EventoOperacional registrar(StatusExecucao statusAnterior, StatusExecucao statusNovo,
	                                           String descricao, String observacao) {
		return new EventoOperacional(statusAnterior, statusNovo, descricao, observacao, Instant.now());
	}
}
