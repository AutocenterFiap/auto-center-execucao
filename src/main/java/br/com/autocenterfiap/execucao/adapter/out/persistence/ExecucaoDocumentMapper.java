package br.com.autocenterfiap.execucao.adapter.out.persistence;

import br.com.autocenterfiap.execucao.adapter.out.persistence.document.EventoOperacionalDocument;
import br.com.autocenterfiap.execucao.adapter.out.persistence.document.ExecucaoDocument;
import br.com.autocenterfiap.execucao.adapter.out.persistence.document.ItensDocument;
import br.com.autocenterfiap.execucao.domain.entity.EventoOperacional;
import br.com.autocenterfiap.execucao.domain.entity.Execucao;
import br.com.autocenterfiap.execucao.domain.entity.Itens;

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
				.numero(execucao.getNumero())
				.clienteId(execucao.getClienteId())
				.veiculoId(execucao.getVeiculoId())
				.problemaRelatado(execucao.getProblemaRelatado())
				.correlationId(execucao.getCorrelationId())
				.status(execucao.getStatus())
				.posicaoFila(execucao.getPosicaoFila())
				.mecanicoId(execucao.getMecanicoId())
				.itens(toItensDocument(execucao.getItens()))
				.motivo(execucao.getMotivo())
				.criadoEm(execucao.getCriadoEm())
				.atualizadoEm(execucao.getAtualizadoEm())
				.finalizadoEm(execucao.getFinalizadoEm())
				.historico(historico)
				.build();
	}

	public static Execucao toDomain(ExecucaoDocument document) {
		List<EventoOperacional> historico = (document.getHistorico() == null
				? List.<EventoOperacionalDocument>of() : document.getHistorico()).stream()
				.map(ExecucaoDocumentMapper::toEventoDomain)
				.collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));

		return Execucao.builder()
				.id(document.getId())
				.osId(document.getOsId())
				.numero(document.getNumero())
				.clienteId(document.getClienteId())
				.veiculoId(document.getVeiculoId())
				.problemaRelatado(document.getProblemaRelatado())
				.correlationId(document.getCorrelationId())
				.status(document.getStatus())
				.posicaoFila(document.getPosicaoFila())
				.mecanicoId(document.getMecanicoId())
				.itens(toItensDomain(document.getItens()))
				.motivo(document.getMotivo())
				.criadoEm(document.getCriadoEm())
				.atualizadoEm(document.getAtualizadoEm())
				.finalizadoEm(document.getFinalizadoEm())
				.historico(historico)
				.build();
	}

	private static ItensDocument toItensDocument(Itens itens) {
		if (itens == null) {
			return null;
		}
		List<ItensDocument.ServicoDocument> servicos = itens.getServicos().stream()
				.map(s -> ItensDocument.ServicoDocument.builder()
						.servicoId(s.servicoId()).descricao(s.descricao()).valor(s.valor()).build())
				.toList();
		List<ItensDocument.ProdutoDocument> produtos = itens.getProdutos().stream()
				.map(p -> ItensDocument.ProdutoDocument.builder()
						.produtoId(p.produtoId()).descricao(p.descricao())
						.quantidade(p.quantidade()).precoUnitario(p.precoUnitario()).build())
				.toList();
		return ItensDocument.builder().servicos(servicos).produtos(produtos).build();
	}

	private static Itens toItensDomain(ItensDocument document) {
		if (document == null) {
			return null;
		}
		List<Itens.ItemServico> servicos = (document.getServicos() == null
				? List.<ItensDocument.ServicoDocument>of() : document.getServicos()).stream()
				.map(s -> new Itens.ItemServico(s.getServicoId(), s.getDescricao(), s.getValor()))
				.toList();
		List<Itens.ItemProduto> produtos = (document.getProdutos() == null
				? List.<ItensDocument.ProdutoDocument>of() : document.getProdutos()).stream()
				.map(p -> new Itens.ItemProduto(p.getProdutoId(), p.getDescricao(),
						p.getQuantidade(), p.getPrecoUnitario()))
				.toList();
		return new Itens(servicos, produtos);
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
