package br.com.autocenterfiap.execucao.adapter.in.controller;

import br.com.autocenterfiap.execucao.adapter.in.dto.EtapaRequest;
import br.com.autocenterfiap.execucao.adapter.in.dto.ExecucaoResponse;
import br.com.autocenterfiap.execucao.adapter.in.dto.FalhaRequest;
import br.com.autocenterfiap.execucao.application.port.in.ConsultarExecucaoUseCase;
import br.com.autocenterfiap.execucao.application.port.in.GerenciarExecucaoUseCase;
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
 * API REST da oficina para acompanhar a fila e avancar as etapas de execucao.
 */
@RestController
@RequestMapping("/execucoes")
@Tag(name = "Execucao", description = "Fila e etapas de execucao da oficina")
public class ExecucaoController {

	private final GerenciarExecucaoUseCase gerenciar;
	private final ConsultarExecucaoUseCase consultar;

	public ExecucaoController(GerenciarExecucaoUseCase gerenciar, ConsultarExecucaoUseCase consultar) {
		this.gerenciar = gerenciar;
		this.consultar = consultar;
	}

	@GetMapping("/{id}")
	@Operation(summary = "Consulta uma execucao pelo id")
	public ExecucaoResponse porId(@PathVariable String id) {
		return ExecucaoResponse.de(consultar.buscarPorId(id));
	}

	@GetMapping("/os/{osId}")
	@Operation(summary = "Consulta a execucao de uma Ordem de Servico")
	public ExecucaoResponse porOsId(@PathVariable String osId) {
		return ExecucaoResponse.de(consultar.buscarPorOsId(osId));
	}

	@GetMapping("/fila")
	@Operation(summary = "Lista as execucoes ativas na fila, por ordem de chegada")
	public List<ExecucaoResponse> fila() {
		return consultar.listarFila().stream().map(ExecucaoResponse::de).toList();
	}

	@PostMapping("/{id}/diagnostico")
	@Operation(summary = "Inicia o diagnostico da execucao")
	public ExecucaoResponse iniciarDiagnostico(@PathVariable String id,
	                                           @RequestBody(required = false) EtapaRequest request) {
		return ExecucaoResponse.de(gerenciar.iniciarDiagnostico(id, observacao(request)));
	}

	@PostMapping("/{id}/reparo")
	@Operation(summary = "Inicia o reparo da execucao")
	public ExecucaoResponse iniciarReparo(@PathVariable String id,
	                                      @RequestBody(required = false) EtapaRequest request) {
		return ExecucaoResponse.de(gerenciar.iniciarReparo(id, observacao(request)));
	}

	@PostMapping("/{id}/finalizacao")
	@Operation(summary = "Finaliza a execucao e publica execucao.finalizada")
	public ExecucaoResponse finalizar(@PathVariable String id,
	                                  @RequestBody(required = false) EtapaRequest request) {
		return ExecucaoResponse.de(gerenciar.finalizar(id, observacao(request)));
	}

	@PostMapping("/{id}/falha")
	@Operation(summary = "Registra falha na execucao e publica execucao.falhou")
	public ExecucaoResponse registrarFalha(@PathVariable String id,
	                                       @Valid @RequestBody FalhaRequest request) {
		return ExecucaoResponse.de(gerenciar.registrarFalha(id, request.motivo()));
	}

	private static String observacao(EtapaRequest request) {
		return request != null ? request.observacao() : null;
	}
}
