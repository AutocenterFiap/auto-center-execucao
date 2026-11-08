package br.com.autocenterfiap.execucao.adapter.out.persistence;

import br.com.autocenterfiap.execucao.adapter.out.persistence.document.EventoOperacionalDocument;
import br.com.autocenterfiap.execucao.adapter.out.persistence.document.ExecucaoDocument;
import br.com.autocenterfiap.execucao.domain.entity.EventoOperacional;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;

import java.util.List;

/**
 * Converte entre o agregado de dominio {@link Execucao} e o documento MongoDB.
 */
public final class ExecucaoDocumentMapper {

	private ExecucaoDocumentMapper() {
	}

	public static ExecucaoDocument toDocument(Execucao execucao) {
		List<EventoOperacionalDocument> historico = execucao.getHistorico().stream()
				.map(ExecucaoDocumentMapper::toEventoDocument)
				.toList();

		return ExecucaoDocument.builder()
				.id(execucao.getId())
				.osId(execucao.getOsId())
				.status(execucao.getStatus())
				.posicaoFila(execucao.getPosicaoFila())
				.motivoFalha(execucao.getMotivoFalha())
				.criadoEm(execucao.getCriadoEm())
				.atualizadoEm(execucao.getAtualizadoEm())
				.finalizadoEm(execucao.getFinalizadoEm())
				.historico(historico)
				.build();
	}

	public static Execucao toDomain(ExecucaoDocument document) {
		List<EventoOperacional> historico = (document.getHistorico() == null ? List.<EventoOperacionalDocument>of()
				: document.getHistorico()).stream()
				.map(ExecucaoDocumentMapper::toEventoDomain)
				.toList();

		return Execucao.reconstituir(
				document.getId(),
				document.getOsId(),
				document.getStatus(),
				document.getPosicaoFila(),
				document.getMotivoFalha(),
				document.getCriadoEm(),
				document.getAtualizadoEm(),
				document.getFinalizadoEm(),
				historico);
	}

	private static EventoOperacionalDocument toEventoDocument(EventoOperacional evento) {
		return EventoOperacionalDocument.builder()
				.statusAnterior(evento.getStatusAnterior())
				.statusNovo(evento.getStatusNovo())
				.descricao(evento.getDescricao())
				.observacao(evento.getObservacao())
				.ocorridoEm(evento.getOcorridoEm())
				.build();
	}

	private static EventoOperacional toEventoDomain(EventoOperacionalDocument document) {
		return new EventoOperacional(
				document.getStatusAnterior(),
				document.getStatusNovo(),
				document.getDescricao(),
				document.getObservacao(),
				document.getOcorridoEm());
	}
}
