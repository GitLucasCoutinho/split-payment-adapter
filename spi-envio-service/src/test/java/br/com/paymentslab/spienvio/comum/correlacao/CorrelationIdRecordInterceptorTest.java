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
        ConsumerRecord<Object, Object> registro = registroCom("corr-abc", "E9999");

        interceptor.intercept(registro, null);

        assertEquals("corr-abc", CorrelationId.atual());
    }

    @Test
    void deve_usar_a_chave_quando_o_cabecalho_nao_vem() {
        ConsumerRecord<Object, Object> registro = registroCom(null, "E9999901020261118014200000000123");

        interceptor.intercept(registro, null);

        assertEquals("E9999901020261118014200000000123", CorrelationId.atual(),
                "reprocessamento que perde cabecalho ainda tem a chave");
    }

    @Test
    void deve_gerar_um_identificador_quando_nao_ha_cabecalho_nem_chave() {
        interceptor.intercept(registroCom(null, null), null);

        assertNotNull(CorrelationId.atual(), "todo log precisa de uma referencia");
        assertFalse(CorrelationId.atual().isBlank());
    }

    @Test
    void deve_ignorar_cabecalho_vazio_e_cair_na_chave() {
        interceptor.intercept(registroCom("   ", "E9999"), null);

        assertEquals("E9999", CorrelationId.atual());
    }

    @Test
    void deve_fechar_o_escopo_depois_de_processar_a_mensagem() {
        ConsumerRecord<Object, Object> registro = registroCom("corr-xyz", "E9999");
        interceptor.intercept(registro, null);

        interceptor.afterRecord(registro, null);

        assertNull(CorrelationId.atual(), "o escopo nao pode vazar para a proxima mensagem");
    }

    private ConsumerRecord<Object, Object> registroCom(String correlacao, String chave) {
        ConsumerRecord<Object, Object> registro = new ConsumerRecord<Object, Object>(
                "segregacao.calculada.v1", 0, 0L, chave, new Object());
        if (correlacao != null) {
            registro.headers().add(CorrelationId.CABECALHO, correlacao.getBytes(StandardCharsets.UTF_8));
        }
        return registro;
    }
}
