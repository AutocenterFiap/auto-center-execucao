package br.com.autocenterfiap.execucao.domain.entity;

import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;
import br.com.autocenterfiap.execucao.domain.exception.ExecucaoInvalidaException;
import br.com.autocenterfiap.execucao.domain.exception.TransicaoInvalidaException;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Raiz de agregado que representa a execucao de uma Ordem de Servico (OS) na oficina.
 *
 * <p>Conduz o diagnostico (ao receber {@code os.criada}) e o reparo (ao receber
 * {@code execucao.iniciar-reparo}), controlando a maquina de estados (ver
 * {@link StatusExecucao}) e mantendo o historico de etapas. Toda transicao registra
 * um {@link EventoOperacional}.
 */
@Getter
public class Execucao {

	private final String id;
	private final Long osId;
	private final Long numero;
	private final Long clienteId;
	private final Long veiculoId;
	private final String problemaRelatado;
	private final String correlationId;
	private StatusExecucao status;
	private Integer posicaoFila;
	private Long mecanicoId;
	private Itens itens;
	private String motivo;
	private final Instant criadoEm;
	private Instant atualizadoEm;
	private Instant finalizadoEm;
	private final List<EventoOperacional> historico;

	@Builder
	private Execucao(String id, Long osId, Long numero, Long clienteId, Long veiculoId,
	                 String problemaRelatado, String correlationId, StatusExecucao status,
	                 Integer posicaoFila, Long mecanicoId, Itens itens, String motivo,
	                 Instant criadoEm, Instant atualizadoEm, Instant finalizadoEm,
	                 List<EventoOperacional> historico) {
		this.id = id;
		this.osId = osId;
		this.numero = numero;
		this.clienteId = clienteId;
		this.veiculoId = veiculoId;
		this.problemaRelatado = problemaRelatado;
		this.correlationId = correlationId;
		this.status = status;
		this.posicaoFila = posicaoFila;
		this.mecanicoId = mecanicoId;
		this.itens = itens;
		this.motivo = motivo;
		this.criadoEm = criadoEm;
		this.atualizadoEm = atualizadoEm;
		this.finalizadoEm = finalizadoEm;
		this.historico = historico != null ? historico : new ArrayList<>();
	}

	/**
	 * Cria uma nova execucao na fila (NA_FILA) a partir do evento {@code os.criada}.
	 */
	public static Execucao criar(Long osId, Long numero, Long clienteId, Long veiculoId,
	                             String problemaRelatado, String correlationId, Integer posicaoFila) {
		if (osId == null) {
			throw new ExecucaoInvalidaException("osId e obrigatorio");
		}
		Instant agora = Instant.now();
		List<EventoOperacional> historico = new ArrayList<>();
		historico.add(new EventoOperacional(null, StatusExecucao.NA_FILA,
				"Execucao criada e adicionada a fila", null, agora));
		return Execucao.builder()
				.osId(osId).numero(numero).clienteId(clienteId).veiculoId(veiculoId)
				.problemaRelatado(problemaRelatado).correlationId(correlationId)
				.status(StatusExecucao.NA_FILA).posicaoFila(posicaoFila)
				.criadoEm(agora).atualizadoEm(agora)
				.historico(historico)
				.build();
	}

	public void iniciarDiagnostico(Long mecanicoId) {
		transitarPara(StatusExecucao.EM_DIAGNOSTICO, "Diagnostico iniciado", null);
		this.mecanicoId = mecanicoId;
	}

	public void concluirDiagnostico(Itens itens) {
		transitarPara(StatusExecucao.DIAGNOSTICO_CONCLUIDO, "Diagnostico concluido", null);
		this.itens = itens;
	}

	public void iniciarReparo(Long mecanicoId) {
		transitarPara(StatusExecucao.EM_EXECUCAO, "Reparo iniciado", null);
		if (mecanicoId != null) {
			this.mecanicoId = mecanicoId;
		}
	}

	public void finalizar() {
		transitarPara(StatusExecucao.FINALIZADA, "Execucao finalizada", null);
		this.finalizadoEm = Instant.now();
	}

	public void falhar(String motivo) {
		this.motivo = motivo;
		transitarPara(StatusExecucao.FALHA, "Execucao falhou", motivo);
		this.finalizadoEm = Instant.now();
	}

	/**
	 * Cancela a execucao (evento {@code os.cancelada}). Idempotente/tolerante: se a
	 * execucao ja esta em estado terminal, nada muda.
	 *
	 * @return {@code true} se cancelou; {@code false} se ja estava terminal
	 */
	public boolean cancelar(String motivo) {
		if (status.ehTerminal()) {
			return false;
		}
		this.motivo = motivo;
		transitarPara(StatusExecucao.CANCELADA, "Execucao cancelada", motivo);
		this.finalizadoEm = Instant.now();
		return true;
	}

	private void transitarPara(StatusExecucao destino, String descricao, String observacao) {
		if (!status.podeTransitarPara(destino)) {
			throw new TransicaoInvalidaException(status, destino);
		}
		EventoOperacional evento = EventoOperacional.registrar(status, destino, descricao, observacao);
		this.status = destino;
		this.atualizadoEm = evento.getOcorridoEm();
		this.historico.add(evento);
	}

	public List<EventoOperacional> getHistorico() {
		return List.copyOf(historico);
	}
}
