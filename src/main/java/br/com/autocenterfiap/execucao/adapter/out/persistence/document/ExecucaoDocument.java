package br.com.autocenterfiap.execucao.adapter.out.persistence.document;

import br.com.autocenterfiap.execucao.domain.enums.StatusExecucao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * Documento MongoDB que representa uma execucao e o seu historico de etapas.
 */
@Document(collection = "execucoes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecucaoDocument {

	@Id
	private String id;

	@Indexed(unique = true)
	private Long osId;

	private Long numero;
	private Long clienteId;
	private Long veiculoId;
	private String problemaRelatado;
	private String correlationId;

	private StatusExecucao status;
	private Integer posicaoFila;
	private Long mecanicoId;
	private ItensDocument itens;
	private String motivo;

	private Instant criadoEm;
	private Instant atualizadoEm;
	private Instant finalizadoEm;
	private List<EventoOperacionalDocument> historico;
}
