package br.com.paymentslab.spienvio.comum.correlacao;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

/**
 * Propaga o identificador de correlacao nas chamadas HTTP de saida.
 *
 * <p>E o mesmo identificador que chegou pelo Kafka, entao o simulador do SPI
 * consegue registrar a chamada com a mesma referencia do evento que a originou.
 */
@Component
public class CorrelationIdHttpInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(
            HttpRequest requisicao,
            byte[] corpo,
            ClientHttpRequestExecution execucao) throws IOException {

        String identificador = CorrelationId.atual();
        if (identificador != null) {
            requisicao.getHeaders().set(CorrelationId.CABECALHO, identificador);
        }
        return execucao.execute(requisicao, corpo);
    }
}
