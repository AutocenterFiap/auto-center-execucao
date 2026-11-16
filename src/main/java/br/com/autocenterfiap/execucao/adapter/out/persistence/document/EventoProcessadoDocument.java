package br.com.autocenterfiap.execucao.adapter.out.persistence.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Registro de um {@code eventId} ja processado, para deduplicacao de mensagens.
 */
@Document(collection = "eventos_processados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventoProcessadoDocument {

	@Id
	private String eventId;
	private Instant processadoEm;
}
