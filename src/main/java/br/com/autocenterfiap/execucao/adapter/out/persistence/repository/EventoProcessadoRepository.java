package br.com.autocenterfiap.execucao.adapter.out.persistence.repository;

import br.com.autocenterfiap.execucao.adapter.out.persistence.document.EventoProcessadoDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EventoProcessadoRepository extends MongoRepository<EventoProcessadoDocument, String> {
}
