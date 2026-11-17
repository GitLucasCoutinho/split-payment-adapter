package br.com.paymentslab.split.dominio.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SegregacaoTest {

    @Test
    void deve_somar_os_tributos() {
        Segregacao segregacao = new Segregacao(
                "E1",
                ValorMonetario.deCentavos(1_000),
                ValorMonetario.deCentavos(10),
                ValorMonetario.deCentavos(90),
                ValorMonetario.deCentavos(900));

        assertEquals(100, segregacao.totalTributos().centavos());
    }

    @Test
    void deve_recusar_segregacao_que_nao_fecha() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> new Segregacao(
                        "E1",
                        ValorMonetario.deCentavos(1_000),
                        ValorMonetario.deCentavos(10),
                        ValorMonetario.deCentavos(90),
                        ValorMonetario.deCentavos(800)));

        assertTrue(erro.getMessage().contains("nao fecha"));
    }
}
