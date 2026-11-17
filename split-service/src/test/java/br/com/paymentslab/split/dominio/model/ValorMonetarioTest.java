package br.com.paymentslab.split.dominio.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ValorMonetarioTest {

    @Test
    void deve_converter_centavos_para_reais_com_duas_casas() {
        assertEquals(new BigDecimal("10.00"), ValorMonetario.deCentavos(1_000).emReais());
        assertEquals(new BigDecimal("0.01"), ValorMonetario.deCentavos(1).emReais());
    }

    @Test
    void deve_recusar_valor_negativo() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> new ValorMonetario(-1));
        assertTrue(erro.getMessage().contains("negativo"));
    }

    @Test
    void deve_somar_e_subtrair() {
        ValorMonetario dez = ValorMonetario.deCentavos(10);
        ValorMonetario cinco = ValorMonetario.deCentavos(5);

        assertEquals(15, dez.mais(cinco).centavos());
        assertEquals(5, dez.menos(cinco).centavos());
    }

    @Test
    void deve_comparar_valores() {
        assertTrue(ValorMonetario.deCentavos(10).maiorQue(ValorMonetario.deCentavos(5)));
        assertFalse(ValorMonetario.deCentavos(5).maiorQue(ValorMonetario.deCentavos(10)));
    }

    @Test
    void zero_deve_ser_zero() {
        assertEquals(0, ValorMonetario.zero().centavos());
    }
}
