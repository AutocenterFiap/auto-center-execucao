package br.com.autocenterfiap.execucao.adapter.in.controller;

import br.com.autocenterfiap.execucao.adapter.in.dto.ExecucaoResponse;
import br.com.autocenterfiap.execucao.adapter.in.dto.FalhaRequest;
import br.com.autocenterfiap.execucao.adapter.in.dto.ItensRequest;
import br.com.autocenterfiap.execucao.application.port.in.ConsultarExecucaoUseCase;
import br.com.autocenterfiap.execucao.application.port.in.GerenciarExecucaoUseCase;
import br.com.autocenterfiap.execucao.application.usecase.MontarItensCatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API REST da oficina: acompanhar a fila e registrar o resultado do diagnostico, a
 * finalizacao e a falha. O inicio do diagnostico e do reparo sao automaticos (via Kafka).
 */
@RestController
@RequestMapping("/execucoes")
@Tag(name = "Execucao", description = "Fila e etapas de execucao da oficina")
public class ExecucaoController {

	private final GerenciarExecucaoUseCase gerenciar;
	private final ConsultarExecucaoUseCase consultar;
	private final MontarItensCatalogoService montarItens;

	public ExecucaoController(GerenciarExecucaoUseCase gerenciar, ConsultarExecucaoUseCase consultar,
	                          MontarItensCatalogoService montarItens) {
		this.gerenciar = gerenciar;
		this.consultar = consultar;
		this.montarItens = montarItens;
	}

	@GetMapping("/os/{osId}")
	@Operation(summary = "Consulta a execucao de uma Ordem de Servico")
	public ExecucaoResponse porOsId(@PathVariable Long osId) {
		return ExecucaoResponse.de(consultar.buscarPorOsId(osId));
	}

	@GetMapping("/fila")
	@Operation(summary = "Lista as execucoes ativas na fila, por ordem de chegada")
	public List<ExecucaoResponse> fila() {
		return consultar.listarFila().stream().map(ExecucaoResponse::de).toList();
	}

	@PostMapping("/{osId}/diagnostico")
	@Operation(summary = "Conclui o diagnostico com os itens e publica diagnostico.concluido",
			description = "Recebe so IDs (e quantidade de produtos); descricao e preco vem do Cadastro. "
					+ "Item inexistente ou inativo: 422. Cadastro indisponivel: 503. Em erro nada e publicado.")
	public ExecucaoResponse concluirDiagnostico(@PathVariable Long osId,
	                                            @Valid @RequestBody ItensRequest request) {
		return ExecucaoResponse.de(gerenciar.concluirDiagnostico(osId, montarItens.montar(request.toSolicitados())));
	}

	@PostMapping("/{osId}/finalizacao")
	@Operation(summary = "Finaliza o reparo e publica execucao.finalizada")
	public ExecucaoResponse finalizar(@PathVariable Long osId) {
		return ExecucaoResponse.de(gerenciar.finalizar(osId));
	}

	@PostMapping("/{osId}/falha")
	@Operation(summary = "Registra falha no reparo e publica execucao.falhou")
	public ExecucaoResponse registrarFalha(@PathVariable Long osId,
	                                       @Valid @RequestBody FalhaRequest request) {
		return ExecucaoResponse.de(gerenciar.registrarFalha(osId, request.motivo()));
	}
}
