package br.com.paymentslab.spienvio.adaptadores.entrada.mensageria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IdentificadorMensagemTest {

    @Test
    void deve_derivar_do_end_to_end_id_quando_ele_tem_tamanho_suficiente() {
        String e2e = "E9999901020260101120000000000001";

        String identificador = IdentificadorMensagem.gerar(e2e, "00038166");

        assertEquals(32, identificador.length());
        assertEquals("M00038166", identificador.substring(0, 9));
        assertEquals(e2e.substring(e2e.length() - 23), identificador.substring(9));
    }

    @Test
    void deve_preencher_com_zero_quando_o_end_to_end_id_e_curto() {
        String identificador = IdentificadorMensagem.gerar("E1", "00038166");

        assertEquals(32, identificador.length());
        assertTrue(identificador.startsWith("M00038166"));
        assertTrue(identificador.endsWith("E1"));
    }
}
