package br.com.paymentslab.carga;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GeradorPagamentoTest {

    private final GeradorPagamento gerador =
            new GeradorPagamento(LocalDateTime.of(2026, 11, 17, 22, 7));

    @Test
    void deve_gerar_identificador_conforme_o_padrao_do_schema() {
        String identificador = gerador.proximoEndToEndId();

        assertTrue(identificador.matches(GeradorPagamento.PADRAO_END_TO_END_ID),
                "fora do padrao do XSD a mensagem e recusada na validacao: " + identificador);
        assertEquals(32, identificador.length(), "o EndToEndId tem exatamente 32 caracteres");
    }

    @Test
    void deve_gerar_identificadores_unicos() {
        String primeiro = gerador.proximoEndToEndId();
        String segundo = gerador.proximoEndToEndId();

        assertNotEquals(primeiro, segundo);
        assertTrue(primeiro.startsWith("E99999010202611172207"),
                "deve carregar ISPB e data com hora");
    }

    @Test
    void deve_gerar_pagamento_com_os_campos_que_o_consumidor_espera() {
        Map<String, Object> pagamento = gerador.proximoPagamento();

        assertTrue(pagamento.containsKey("endToEndId"));
        assertTrue(pagamento.containsKey("valor"));
        assertTrue(pagamento.containsKey("pagador"));
        assertTrue(pagamento.containsKey("recebedor"));
        assertTrue(pagamento.containsKey("finalidade"));
        assertTrue(pagamento.containsKey("liquidadoEm"));
        assertTrue(centavos(pagamento) >= 100, "o valor precisa ser positivo");
    }

    @Test
    void o_identificador_do_pagamento_tambem_deve_seguir_o_padrao() {
        String identificador = (String) gerador.proximoPagamento().get("endToEndId");

        assertTrue(identificador.matches(GeradorPagamento.PADRAO_END_TO_END_ID));
    }

    @Test
    void o_valor_deve_variar_entre_pagamentos() {
        assertNotEquals(centavos(gerador.proximoPagamento()), centavos(gerador.proximoPagamento()),
                "carga com valor fixo nao exercita o calculo");
    }

    @SuppressWarnings("unchecked")
    private int centavos(Map<String, Object> pagamento) {
        Map<String, Object> valor = (Map<String, Object>) pagamento.get("valor");
        return ((Number) valor.get("centavos")).intValue();
    }
}
