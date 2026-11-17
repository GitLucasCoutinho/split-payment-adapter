package br.com.paymentslab.spienvio;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Sobe o contexto completo sem depender de broker.
 *
 * <p>Este teste existe porque erro de configuracao, como o padrao do logback, so
 * aparece na subida da aplicacao. Sem ele, o problema seria descoberto em
 * execucao, e nao no build.
 */
@SpringBootTest(properties = "spring.kafka.listener.auto-startup=false")
class ApplicationContextTest {

    @Test
    void deve_subir_o_contexto_com_as_configuracoes_atuais() {
        // Se o logback, o codec ou a injecao estiverem errados, o contexto nao sobe.
    }
}
