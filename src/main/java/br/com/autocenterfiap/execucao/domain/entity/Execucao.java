package br.com.autocenterfiap.execucao.domain.entity;

import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;
import br.com.autocenterfiap.execucao.domain.exception.ExecucaoInvalidaException;
import br.com.autocenterfiap.execucao.domain.exception.TransicaoInvalidaException;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Raiz de agregado que representa a execucao de uma Ordem de Servico (OS) na oficina.
 *
 * <p>Controla a maquina de estados (ver {@link StatusExecucao}) e mantem o historico
 * de etapas/eventos operacionais. Toda transicao valida registra um
 * {@link EventoOperacional}.
 */
@Getter
public class Execucao {

	private final String id;
	private final String osId;
	private StatusExecucao status;
	private Integer posicaoFila;
	private String motivoFalha;
	private final Instant criadoEm;
	private Instant atualizadoEm;
	private Instant finalizadoEm;
	private final List<EventoOperacional> historico;

	private Execucao(String id, String osId, StatusExecucao status, Integer posicaoFila,
	                 String motivoFalha, Instant criadoEm, Instant atualizadoEm, Instant finalizadoEm,
	                 List<EventoOperacional> historico) {
		this.id = id;
		this.osId = osId;
		this.status = status;
		this.posicaoFila = posicaoFila;
		this.motivoFalha = motivoFalha;
		this.criadoEm = criadoEm;
		this.atualizadoEm = atualizadoEm;
		this.finalizadoEm = finalizadoEm;
		this.historico = historico;
	}

	/**
	 * Cria uma nova execucao ja na fila (estado inicial {@code NA_FILA}), disparada
	 * pelo recebimento do evento {@code pagamento.confirmado}.
	 */
	public static Execucao criarNaFila(String osId, Integer posicaoFila) {
		if (osId == null || osId.isBlank()) {
			throw new ExecucaoInvalidaException("osId e obrigatorio");
		}
		Instant agora = Instant.now();
		List<EventoOperacional> historico = new ArrayList<>();
		historico.add(new EventoOperacional(null, StatusExecucao.NA_FILA,
				"Execucao criada e adicionada a fila", null, agora));
		return new Execucao(null, osId, StatusExecucao.NA_FILA, posicaoFila, null,
				agora, agora, null, historico);
	}

	/**
	 * Reconstroi uma execucao a partir dos dados persistidos (usado pela camada de
	 * persistencia). Nao gera novos eventos.
	 */
	public static Execucao reconstituir(String id, String osId, StatusExecucao status, Integer posicaoFila,
	                                     String motivoFalha, Instant criadoEm, Instant atualizadoEm,
	                                     Instant finalizadoEm, List<EventoOperacional> historico) {
		if (osId == null || osId.isBlank()) {
			throw new ExecucaoInvalidaException("osId e obrigatorio");
		}
		if (status == null) {
			throw new ExecucaoInvalidaException("status e obrigatorio");
		}
		return new Execucao(id, osId, status, posicaoFila, motivoFalha, criadoEm, atualizadoEm,
				finalizadoEm, historico != null ? new ArrayList<>(historico) : new ArrayList<>());
	}

	public void iniciarDiagnostico(String observacao) {
		transitarPara(StatusExecucao.EM_DIAGNOSTICO, "Diagnostico iniciado", observacao);
	}

	public void iniciarReparo(String observacao) {
		transitarPara(StatusExecucao.EM_EXECUCAO, "Reparo iniciado", observacao);
	}

	public void finalizar(String observacao) {
		transitarPara(StatusExecucao.FINALIZADA, "Execucao finalizada", observacao);
		this.finalizadoEm = Instant.now();
	}

	public void falhar(String motivo) {
		this.motivoFalha = motivo;
		transitarPara(StatusExecucao.FALHA, "Execucao falhou", motivo);
		this.finalizadoEm = Instant.now();
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

	/**
	 * Historico imutavel de eventos operacionais.
	 */
	public List<EventoOperacional> getHistorico() {
		return List.copyOf(historico);
	}
}
