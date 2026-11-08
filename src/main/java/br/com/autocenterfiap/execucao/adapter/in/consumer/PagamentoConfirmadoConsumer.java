package br.com.autocenterfiap.execucao.adapter.in.consumer;

import br.com.autocenterfiap.execucao.application.dto.IniciarExecucaoCommand;
import br.com.autocenterfiap.execucao.application.port.in.IniciarExecucaoUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

/**
 * Consome {@code pagamento.confirmado} e inicia a execucao da OS.
 *
 * <p>Em caso de erro, o Spring Kafka reencaminha para topicos de retry e, esgotadas as
 * tentativas, para a dead-letter topic ({@code <topico>.DLT}).
 */
@Component
public class PagamentoConfirmadoConsumer {

	private static final Logger log = LoggerFactory.getLogger(PagamentoConfirmadoConsumer.class);

	private final IniciarExecucaoUseCase iniciarExecucaoUseCase;

	public PagamentoConfirmadoConsumer(IniciarExecucaoUseCase iniciarExecucaoUseCase) {
		this.iniciarExecucaoUseCase = iniciarExecucaoUseCase;
	}

	@RetryableTopic(
			attempts = "4",
			backoff = @Backoff(delay = 2000, multiplier = 2.0),
			dltStrategy = DltStrategy.FAIL_ON_ERROR,
			autoCreateTopics = "true")
	@KafkaListener(
			topics = "${execucao.kafka.topics.pagamento-confirmado}",
			groupId = "${spring.kafka.consumer.group-id}")
	public void onPagamentoConfirmado(PagamentoConfirmadoEvent evento) {
		log.info("Recebido pagamento.confirmado para OS {}", evento.osId());
		iniciarExecucaoUseCase.iniciar(new IniciarExecucaoCommand(evento.osId()));
	}

	@DltHandler
	public void tratarDlt(PagamentoConfirmadoEvent evento) {
		log.error("Evento pagamento.confirmado da OS {} enviado para a DLT apos esgotar as tentativas",
				evento.osId());
	}
}
