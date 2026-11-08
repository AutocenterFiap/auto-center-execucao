package br.com.autocenterfiap.execucao.adapter.in.dto;

import br.com.autocenterfiap.execucao.domain.entity.Execucao;
import br.com.autocenterfiap.execucao.domain.entity.Itens;
import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ExecucaoResponse(
		String id,
		Long osId,
		Long numero,
		Long clienteId,
		Long veiculoId,
		String problemaRelatado,
		String correlationId,
		StatusExecucao status,
		Integer posicaoFila,
		Long mecanicoId,
		String motivo,
		ItensResponse itens,
		Instant criadoEm,
		Instant atualizadoEm,
		Instant finalizadoEm,
		List<EventoResponse> historico) {

	public record ItensResponse(List<ServicoResponse> servicos, List<ProdutoResponse> produtos) {
	}

	public record ServicoResponse(Long servicoId, String descricao, BigDecimal valor) {
	}

	public record ProdutoResponse(Long produtoId, String descricao, int quantidade, BigDecimal precoUnitario) {
	}

	public static ExecucaoResponse de(Execucao execucao) {
		List<EventoResponse> historico = execucao.getHistorico().stream()
				.map(EventoResponse::de)
				.toList();

		return new ExecucaoResponse(
				execucao.getId(),
				execucao.getOsId(),
				execucao.getNumero(),
				execucao.getClienteId(),
				execucao.getVeiculoId(),
				execucao.getProblemaRelatado(),
				execucao.getCorrelationId(),
				execucao.getStatus(),
				execucao.getPosicaoFila(),
				execucao.getMecanicoId(),
				execucao.getMotivo(),
				toItens(execucao.getItens()),
				execucao.getCriadoEm(),
				execucao.getAtualizadoEm(),
				execucao.getFinalizadoEm(),
				historico);
	}

	private static ItensResponse toItens(Itens itens) {
		if (itens == null) {
			return null;
		}
		List<ServicoResponse> servicos = itens.getServicos().stream()
				.map(s -> new ServicoResponse(s.servicoId(), s.descricao(), s.valor()))
				.toList();
		List<ProdutoResponse> produtos = itens.getProdutos().stream()
				.map(p -> new ProdutoResponse(p.produtoId(), p.descricao(), p.quantidade(), p.precoUnitario()))
				.toList();
		return new ItensResponse(servicos, produtos);
	}
}
