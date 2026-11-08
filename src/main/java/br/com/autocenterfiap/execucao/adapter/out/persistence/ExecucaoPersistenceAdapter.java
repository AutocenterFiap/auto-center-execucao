package br.com.autocenterfiap.execucao.adapter.out.persistence;

import br.com.autocenterfiap.execucao.adapter.out.persistence.document.ExecucaoDocument;
import br.com.autocenterfiap.execucao.adapter.out.persistence.repository.ExecucaoMongoRepository;
import br.com.autocenterfiap.execucao.application.port.out.ExecucaoRepositoryPort;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;
import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Implementacao da porta de persistencia usando MongoDB.
 */
@Component
public class ExecucaoPersistenceAdapter implements ExecucaoRepositoryPort {

	private static final List<StatusExecucao> STATUS_ATIVOS =
			List.of(StatusExecucao.NA_FILA, StatusExecucao.EM_DIAGNOSTICO, StatusExecucao.EM_EXECUCAO);

	private final ExecucaoMongoRepository repository;

	public ExecucaoPersistenceAdapter(ExecucaoMongoRepository repository) {
		this.repository = repository;
	}

	@Override
	public Execucao salvar(Execucao execucao) {
		ExecucaoDocument salvo = repository.save(ExecucaoDocumentMapper.toDocument(execucao));
		return ExecucaoDocumentMapper.toDomain(salvo);
	}

	@Override
	public Optional<Execucao> buscarPorId(String id) {
		return repository.findById(id).map(ExecucaoDocumentMapper::toDomain);
	}

	@Override
	public Optional<Execucao> buscarPorOsId(String osId) {
		return repository.findByOsId(osId).map(ExecucaoDocumentMapper::toDomain);
	}

	@Override
	public List<Execucao> listarFila() {
		return repository.findByStatusInOrderByPosicaoFilaAsc(STATUS_ATIVOS).stream()
				.map(ExecucaoDocumentMapper::toDomain)
				.toList();
	}

	@Override
	public long proximaPosicaoFila() {
		return repository.count() + 1;
	}
}
