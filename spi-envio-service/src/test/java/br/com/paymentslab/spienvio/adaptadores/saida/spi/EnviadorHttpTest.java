package br.com.paymentslab.spienvio.adaptadores.saida.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import br.com.paymentslab.spienvio.comum.correlacao.CorrelationId;
import br.com.paymentslab.spienvio.comum.correlacao.CorrelationIdHttpInterceptor;
import br.com.paymentslab.spienvio.dominio.porta.saida.Protocolo;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class EnviadorHttpTest {

    private static final String URL = "http://localhost:8083/spi/mensagens";

    @Test
    void deve_enviar_o_xml_e_devolver_o_protocolo() {
        RestClient.Builder construtor = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(construtor).build();

        servidor.expect(requestTo(URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string("<Envelope/>"))
                .andExpect(header(CorrelationId.CABECALHO, "corr-1"))
                .andRespond(withSuccess(
                        "{\"protocolo\":\"SIM-2026-000001\",\"recebidoEm\":\"2026-01-01T08:30:12Z\"}",
                        MediaType.APPLICATION_JSON));

        EnviadorHttp enviador = new EnviadorHttp(construtor, new CorrelationIdHttpInterceptor(), URL);

        CorrelationId.definir("corr-1");
        Protocolo protocolo;
        try {
            protocolo = enviador.enviar("<Envelope/>");
        } finally {
            CorrelationId.limpar();
        }

        assertEquals("SIM-2026-000001", protocolo.numero());
        assertNotNull(protocolo.recebidoEm());
        servidor.verify();
    }
}
