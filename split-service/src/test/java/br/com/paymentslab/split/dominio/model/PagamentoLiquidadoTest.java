package br.com.paymentslab.split.dominio.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class PagamentoLiquidadoTest {

    @Test
    void deve_recusar_end_to_end_id_vazio() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> new PagamentoLiquidado(" ", valor(), pagador(), recebedor(), "IPAY", "x", Instant.now()));
        assertTrue(erro.getMessage().contains("EndToEndId"));
    }

    @Test
    void deve_recusar_valor_zero() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> new PagamentoLiquidado("E1", ValorMonetario.zero(), pagador(), recebedor(),
                        "IPAY", "x", Instant.now()));
        assertTrue(erro.getMessage().contains("Valor"));
    }

    @Test
    void deve_recusar_data_ausente() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> new PagamentoLiquidado("E1", valor(), pagador(), recebedor(),
                        "IPAY", "x", null));
        assertTrue(erro.getMessage().contains("liquidacao"));
    }

    @Test
    void deve_guardar_os_dados_recebidos() {
        PagamentoLiquidado pagamento = new PagamentoLiquidado(
                "E1", valor(), pagador(), recebedor(), "IPAY", "mensagem",
                Instant.parse("2026-01-01T00:00:00Z"));

        assertEquals("E1", pagamento.endToEndId());
        assertEquals(100, pagamento.valor().centavos());
        assertEquals("00038166", pagamento.pagador().ispb());
    }

    @Test
    void participante_deve_recusar_ispb_vazio() {
        assertThrows(IllegalArgumentException.class,
                () -> new Participante("", "Nome", "70000000000", "0001", "1", "CACC"));
    }

    private ValorMonetario valor() {
        return ValorMonetario.deCentavos(100);
    }

    private Participante pagador() {
        return new Participante("00038166", "Fulano", "70000000000", "0001", "500000", "CACC");
    }

    private Participante recebedor() {
        return new Participante("99999010", "Beltrano", "80000000000", "0001", "600000", "SVGS");
    }
}
