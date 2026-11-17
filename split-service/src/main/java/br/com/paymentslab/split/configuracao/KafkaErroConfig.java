package br.com.paymentslab.split.configuracao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Tratamento de erro do consumidor.
 *
 * <p>Falha de processamento e reprocessada algumas vezes, com intervalo. Depois
 * disso a mensagem vai para o topico de erro, mantendo cabecalhos e chave, para
 * analise posterior.
 *
 * <p>Sem isso, uma mensagem invalida ficaria reprocessando para sempre e travaria
 * a particao, porque o offset nunca avancaria.
 */
@Configuration
public class KafkaErroConfig {

    private static final long INTERVALO_ENTRE_TENTATIVAS_MS = 1_000L;
    private static final long TENTATIVAS_EXTRA = 3L;

    @Bean
    public DefaultErrorHandler manipuladorDeErro(KafkaTemplate<String, Object> template) {
        DeadLetterPublishingRecoverer publicadorDeErro = new DeadLetterPublishingRecoverer(template);
        return new DefaultErrorHandler(
                publicadorDeErro,
                new FixedBackOff(INTERVALO_ENTRE_TENTATIVAS_MS, TENTATIVAS_EXTRA));
    }
}
