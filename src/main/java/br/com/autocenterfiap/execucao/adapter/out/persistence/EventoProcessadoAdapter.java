package br.com.autocenterfiap.execucao.adapter.out.persistence;

import br.com.autocenterfiap.execucao.adapter.out.persistence.document.EventoProcessadoDocument;
import br.com.autocenterfiap.execucao.adapter.out.persistence.repository.EventoProcessadoRepository;
import br.com.autocenterfiap.execucao.application.port.out.EventoProcessadoPort;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Deduplicacao por {@code eventId} na colecao {@code eventos_processados}.
 */
@Component
public class EventoProcessadoAdapter implements EventoProcessadoPort {

	private final EventoProcessadoRepository repository;

	public EventoProcessadoAdapter(EventoProcessadoRepository repository) {
		this.repository = repository;
	}

	@Override
	public boolean jaProcessado(String eventId) {
		return repository.existsById(eventId);
	}

	@Override
	public void marcarProcessado(String eventId) {
		repository.save(new EventoProcessadoDocument(eventId, Instant.now()));
	}
}
