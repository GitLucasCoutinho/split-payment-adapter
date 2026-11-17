package br.com.paymentslab.spienvio.comum.correlacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.URI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

class CorrelationIdHttpInterceptorTest {

    private final CorrelationIdHttpInterceptor interceptor = new CorrelationIdHttpInterceptor();

    @AfterEach
    void limparEscopo() {
        CorrelationId.limpar();
    }

    @Test
    void deve_propagar_o_identificador_na_chamada_de_saida() throws Exception {
        CorrelationId.definir("corr-123");

        MockClientHttpRequest requisicao = requisicao();

        interceptor.intercept(requisicao, new byte[0], execucaoOk());

        assertEquals("corr-123", requisicao.getHeaders().getFirst(CorrelationId.CABECALHO));
    }

    @Test
    void nao_deve_enviar_cabecalho_quando_nao_ha_correlacao() throws Exception {
        MockClientHttpRequest requisicao = requisicao();

        interceptor.intercept(requisicao, new byte[0], execucaoOk());

        assertNull(requisicao.getHeaders().getFirst(CorrelationId.CABECALHO));
    }

    private MockClientHttpRequest requisicao() {
        return new MockClientHttpRequest(HttpMethod.POST, URI.create("http://localhost:8083/spi/mensagens"));
    }

    private ClientHttpRequestExecution execucaoOk() {
        return (requisicao, corpo) -> new MockClientHttpResponse(new byte[0], HttpStatus.OK);
    }
}
