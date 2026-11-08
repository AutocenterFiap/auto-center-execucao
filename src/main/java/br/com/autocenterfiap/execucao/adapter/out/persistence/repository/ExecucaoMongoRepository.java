package br.com.autocenterfiap.execucao.adapter.out.persistence.repository;

import br.com.autocenterfiap.execucao.adapter.out.persistence.document.ExecucaoDocument;
import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ExecucaoMongoRepository extends MongoRepository<ExecucaoDocument, String> {

	Optional<ExecucaoDocument> findByOsId(String osId);

	List<ExecucaoDocument> findByStatusInOrderByPosicaoFilaAsc(Collection<StatusExecucao> status);
}
