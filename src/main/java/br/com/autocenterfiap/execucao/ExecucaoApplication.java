package br.com.autocenterfiap.execucao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Microsservico de Execucao da Oficina (Fase 4).
 *
 * <p>Responsavel pela fila de servicos da oficina: controla diagnostico, inicio e
 * finalizacao dos reparos. Consome o evento {@code pagamento.confirmado} e publica
 * {@code execucao.iniciada}, {@code execucao.finalizada} e {@code execucao.falhou}.
 *
 * <p>Persistencia NoSQL (MongoDB) para o historico de etapas e eventos operacionais.
 */
@SpringBootApplication
@EnableKafka
public class ExecucaoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExecucaoApplication.class, args);
	}

}
