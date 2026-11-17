package br.com.autocenterfiap.execucao.adapter.in.controller;

import br.com.autocenterfiap.execucao.adapter.in.dto.ExecucaoResponse;
import br.com.autocenterfiap.execucao.adapter.in.dto.FalhaRequest;
import br.com.autocenterfiap.execucao.adapter.in.dto.ItensRequest;
import br.com.autocenterfiap.execucao.application.port.in.ConsultarExecucaoUseCase;
import br.com.autocenterfiap.execucao.application.port.in.GerenciarExecucaoUseCase;
import br.com.autocenterfiap.execucao.adapter.in.handler.ErroResponse;
import br.com.autocenterfiap.execucao.application.usecase.MontarItensCatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Execução", description = "Fila da oficina e registro das etapas de diagnóstico e reparo")
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
	@Operation(summary = "Consultar execução de uma OS",
			description = "Retorna a execução com status, posição na fila, itens do diagnóstico e histórico")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Execução encontrada"),
			@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
					content = @Content),
			@ApiResponse(responseCode = "403", description = "Token sem o perfil interno (funcionário)",
					content = @Content),
			@ApiResponse(responseCode = "404", description = "Execução não encontrada para a OS",
					content = @Content(schema = @Schema(implementation = ErroResponse.class)))
	})
	public ExecucaoResponse porOsId(
			@Parameter(description = "Identificador da OS", example = "42") @PathVariable Long osId) {
		return ExecucaoResponse.de(consultar.buscarPorOsId(osId));
	}

	@GetMapping("/fila")
	@Operation(summary = "Listar fila da oficina",
			description = "Lista as execuções ativas por ordem de chegada; finalizadas, com falha e canceladas "
					+ "não aparecem")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Fila retornada"),
			@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
					content = @Content),
			@ApiResponse(responseCode = "403", description = "Token sem o perfil interno (funcionário)",
					content = @Content)
	})
	public List<ExecucaoResponse> fila() {
		return consultar.listarFila().stream().map(ExecucaoResponse::de).toList();
	}

	@PostMapping("/{osId}/diagnostico")
	@Operation(summary = "Concluir diagnóstico",
			description = "Recebe apenas os IDs dos serviços e produtos, com a quantidade dos produtos. Descrição "
					+ "e preço são buscados no Cadastro e o evento diagnostico.concluido é publicado para a Saga "
					+ "gerar o orçamento. Em caso de erro nada é publicado")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Diagnóstico concluído"),
			@ApiResponse(responseCode = "400", description = "Corpo inválido ou campos obrigatórios ausentes",
					content = @Content(schema = @Schema(implementation = ErroResponse.class))),
			@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
					content = @Content),
			@ApiResponse(responseCode = "403", description = "Token sem o perfil interno (funcionário)",
					content = @Content),
			@ApiResponse(responseCode = "404", description = "Execução não encontrada para a OS",
					content = @Content(schema = @Schema(implementation = ErroResponse.class))),
			@ApiResponse(responseCode = "409", description = "A execução não está EM_DIAGNOSTICO",
					content = @Content(schema = @Schema(implementation = ErroResponse.class))),
			@ApiResponse(responseCode = "422", description = "Serviço ou produto inexistente ou inativo no Cadastro",
					content = @Content(schema = @Schema(implementation = ErroResponse.class))),
			@ApiResponse(responseCode = "503", description = "Serviço de Cadastro indisponível",
					content = @Content(schema = @Schema(implementation = ErroResponse.class)))
	})
	public ExecucaoResponse concluirDiagnostico(
			@Parameter(description = "Identificador da OS", example = "42") @PathVariable Long osId,
			@Valid @RequestBody ItensRequest request) {
		return ExecucaoResponse.de(gerenciar.concluirDiagnostico(osId, montarItens.montar(request.toSolicitados())));
	}

	@PostMapping("/{osId}/finalizacao")
	@Operation(summary = "Finalizar reparo",
			description = "Conclui o reparo e publica execucao.finalizada; a OS passa para CONCLUIDA")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Reparo finalizado"),
			@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
					content = @Content),
			@ApiResponse(responseCode = "403", description = "Token sem o perfil interno (funcionário)",
					content = @Content),
			@ApiResponse(responseCode = "404", description = "Execução não encontrada para a OS",
					content = @Content(schema = @Schema(implementation = ErroResponse.class))),
			@ApiResponse(responseCode = "409", description = "A execução não está EM_EXECUCAO",
					content = @Content(schema = @Schema(implementation = ErroResponse.class)))
	})
	public ExecucaoResponse finalizar(
			@Parameter(description = "Identificador da OS", example = "42") @PathVariable Long osId) {
		return ExecucaoResponse.de(gerenciar.finalizar(osId));
	}

	@PostMapping("/{osId}/falha")
	@Operation(summary = "Registrar falha no reparo",
			description = "Registra a falha com o motivo e publica execucao.falhou; a Saga solicita o estorno "
					+ "do pagamento ao Billing")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Falha registrada"),
			@ApiResponse(responseCode = "400", description = "Motivo ausente",
					content = @Content(schema = @Schema(implementation = ErroResponse.class))),
			@ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
					content = @Content),
			@ApiResponse(responseCode = "403", description = "Token sem o perfil interno (funcionário)",
					content = @Content),
			@ApiResponse(responseCode = "404", description = "Execução não encontrada para a OS",
					content = @Content(schema = @Schema(implementation = ErroResponse.class))),
			@ApiResponse(responseCode = "409", description = "A execução não está EM_EXECUCAO",
					content = @Content(schema = @Schema(implementation = ErroResponse.class)))
	})
	public ExecucaoResponse registrarFalha(
			@Parameter(description = "Identificador da OS", example = "42") @PathVariable Long osId,
			@Valid @RequestBody FalhaRequest request) {
		return ExecucaoResponse.de(gerenciar.registrarFalha(osId, request.motivo()));
	}
}
