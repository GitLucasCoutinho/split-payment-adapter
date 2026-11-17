package br.com.paymentslab.spienvio.comum.correlacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class CorrelationIdRecordInterceptorTest {

    private final CorrelationIdRecordInterceptor interceptor = new CorrelationIdRecordInterceptor();

    @AfterEach
    void limparEscopo() {
        CorrelationId.limpar();
    }

    @Test
    void deve_usar_o_identificador_que_veio_no_cabecalho() {
        ConsumerRecord<Object, Object> registro = registroCom("corr-abc");

        interceptor.intercept(registro, null);

        assertEquals("corr-abc", CorrelationId.atual());
    }

    @Test
    void deve_gerar_um_identificador_quando_o_cabecalho_nao_vem() {
        interceptor.intercept(registroCom(null), null);

        assertNotNull(CorrelationId.atual(), "todo log precisa de uma referencia");
        assertFalse(CorrelationId.atual().isBlank());
    }

    @Test
    void deve_ignorar_cabecalho_vazio() {
        interceptor.intercept(registroCom("   "), null);

        assertNotNull(CorrelationId.atual());
        assertFalse(CorrelationId.atual().isBlank());
    }

    @Test
    void deve_fechar_o_escopo_depois_de_processar_a_mensagem() {
        ConsumerRecord<Object, Object> registro = registroCom("corr-xyz");
        interceptor.intercept(registro, null);

        interceptor.afterRecord(registro, null);

        assertNull(CorrelationId.atual(), "o escopo nao pode vazar para a proxima mensagem");
    }

    private ConsumerRecord<Object, Object> registroCom(String correlacao) {
        ConsumerRecord<Object, Object> registro = new ConsumerRecord<Object, Object>(
                "segregacao.calculada.v1", 0, 0L, "E9999", new Object());
        if (correlacao != null) {
            registro.headers().add(CorrelationId.CABECALHO, correlacao.getBytes(StandardCharsets.UTF_8));
        }
        return registro;
    }
}
