package br.com.paymentslab.spienvio.adaptadores.saida.spi;

import br.com.paymentslab.spienvio.comum.correlacao.CorrelationIdHttpInterceptor;
import br.com.paymentslab.spienvio.dominio.porta.saida.EnvioSpi;
import br.com.paymentslab.spienvio.dominio.porta.saida.Protocolo;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Adaptador de saida: envia o XML ao simulador do SPI por HTTP.
 *
 * O simulador valida contra o mesmo XSD antes de responder. Se o XML estiver
 * errado, o erro aparece aqui, e nao depois.
 */
@Component
public class EnviadorHttp implements EnvioSpi {

    private final RestClient cliente;
    private final String url;

    public EnviadorHttp(
            RestClient.Builder construtor,
            CorrelationIdHttpInterceptor correlacao,
            @Value("${split.spi.url}") String url) {
        this.cliente = construtor.requestInterceptor(correlacao).build();
        this.url = url;
    }

    @Override
    public Protocolo enviar(String xmlAssinado) {
        Resposta resposta = cliente.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_XML)
                .body(xmlAssinado)
                .retrieve()
                .body(Resposta.class);

        if (resposta == null) {
            throw new IllegalStateException("Simulador do SPI nao devolveu resposta");
        }
        return new Protocolo(resposta.protocolo(), resposta.recebidoEm());
    }

    /**
     * Formato da resposta do simulador.
     */
    public record Resposta(String protocolo, Instant recebidoEm) {
    }
}
