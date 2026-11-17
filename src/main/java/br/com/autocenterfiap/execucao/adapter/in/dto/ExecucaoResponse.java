package br.com.autocenterfiap.execucao.adapter.in.dto;

import br.com.autocenterfiap.execucao.domain.entity.Execucao;
import br.com.autocenterfiap.execucao.domain.entity.Itens;
import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Schema(description = "Execução de uma ordem de serviço na oficina")
public record ExecucaoResponse(
		@Schema(description = "Identificador da execução", example = "6738f1c2a9d4e51b2c3d4e5f") String id,
		@Schema(description = "Identificador da OS no serviço de OS", example = "42") Long osId,
		@Schema(description = "Número sequencial da OS", example = "1001") Long numero,
		@Schema(description = "Identificador do cliente", example = "1") Long clienteId,
		@Schema(description = "Identificador do veículo", example = "1") Long veiculoId,
		@Schema(description = "Problema relatado na abertura da OS", example = "Barulho ao frear")
		String problemaRelatado,
		@Schema(description = "Identificador de correlação da Saga",
				example = "3f1c2b9e-7a44-4c1e-9d0b-1a2b3c4d5e6f") String correlationId,
		@Schema(description = "Status da execução", example = "EM_EXECUCAO") StatusExecucao status,
		@Schema(description = "Posição na fila da oficina por ordem de chegada", example = "3") Integer posicaoFila,
		@Schema(description = "Identificador do mecânico responsável", example = "10") Long mecanicoId,
		@Schema(description = "Motivo informado na falha do reparo", example = "Peca com defeito") String motivo,
		@Schema(description = "Itens definidos no diagnóstico") ItensResponse itens,
		@Schema(description = "Entrada na fila", example = "2026-11-17T12:00:00Z") Instant criadoEm,
		@Schema(description = "Última alteração", example = "2026-11-17T12:30:00Z") Instant atualizadoEm,
		@Schema(description = "Conclusão do reparo, com sucesso ou falha", example = "2026-11-17T15:00:00Z")
		Instant finalizadoEm,
		@Schema(description = "Eventos operacionais em ordem cronológica") List<EventoResponse> historico) {

	@Schema(name = "ItensExecucaoResponse",
			description = "Serviços e produtos com descrição e preço vindos do Cadastro")
	public record ItensResponse(
			@Schema(description = "Serviços a executar") List<ServicoResponse> servicos,
			@Schema(description = "Produtos e insumos utilizados") List<ProdutoResponse> produtos) {
	}

	@Schema(name = "ServicoExecucaoResponse", description = "Serviço do diagnóstico")
	public record ServicoResponse(
			@Schema(description = "Identificador do serviço no Cadastro", example = "3") Long servicoId,
			@Schema(description = "Descrição do serviço", example = "Troca de pastilhas de freio") String descricao,
			@Schema(description = "Valor do serviço", example = "180.00") BigDecimal valor) {
	}

	@Schema(name = "ProdutoExecucaoResponse", description = "Produto ou insumo do diagnóstico")
	public record ProdutoResponse(
			@Schema(description = "Identificador do produto no Cadastro", example = "7") Long produtoId,
			@Schema(description = "Descrição do produto", example = "Pastilha de freio dianteira") String descricao,
			@Schema(description = "Quantidade utilizada", example = "2") int quantidade,
			@Schema(description = "Preço unitário", example = "129.95") BigDecimal precoUnitario) {
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
