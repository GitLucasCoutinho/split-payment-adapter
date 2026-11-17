package br.com.paymentslab.spienvio.configuracao;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Tratamento de erro do consumidor.
 *
 * <p>Aqui o erro e mais provavel do que nos outros servicos, porque o envio e
 * uma chamada HTTP. Se o SPI estiver fora, a mensagem e reprocessada algumas
 * vezes e, se continuar falhando, vai para o topico de erro.
 *
 * <p>O topico de erro recebe o mesmo nome com o sufixo {@code .DLT}, mantendo
 * chave e cabecalhos, inclusive o identificador de correlacao.
 */
@Configuration
public class KafkaErroConfig {

    private static final long INTERVALO_ENTRE_TENTATIVAS_MS = 2_000L;
    private static final long TENTATIVAS_EXTRA = 3L;

    @Bean
    public DefaultErrorHandler manipuladorDeErro(KafkaTemplate<String, Object> template) {
        DeadLetterPublishingRecoverer publicadorDeErro = new DeadLetterPublishingRecoverer(template);
        return new DefaultErrorHandler(
                publicadorDeErro,
                new FixedBackOff(INTERVALO_ENTRE_TENTATIVAS_MS, TENTATIVAS_EXTRA));
    }
}
