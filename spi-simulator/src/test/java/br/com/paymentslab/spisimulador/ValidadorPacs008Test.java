package br.com.paymentslab.spisimulador;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ValidadorPacs008Test {

    private final ValidadorPacs008 validador = new ValidadorPacs008();

    @Test
    void deve_aceitar_mensagem_conforme_e_extrair_o_end_to_end_id() throws Exception {
        String xml = mensagemAssinada();

        assertDoesNotThrow(() -> validador.validar(xml));
        assertEquals("E9999901020260101120000000000001", validador.endToEndId(xml));
    }

    @Test
    void deve_recusar_mensagem_fora_do_contrato() {
        assertThrows(IllegalStateException.class, () -> validador.validar("<Envelope/>"));
    }

    @Test
    void deve_recusar_mensagem_sem_end_to_end_id() {
        assertThrows(IllegalStateException.class, () -> validador.endToEndId("<Envelope/>"));
    }

    @Test
    void deve_recusar_xml_malformado_ao_extrair_identificador() {
        assertThrows(IllegalStateException.class, () -> validador.endToEndId("<Envelope>"));
    }

    private String mensagemAssinada() throws Exception {
        try (InputStream entrada =
                     getClass().getResourceAsStream("/exemplo/pacs.008_assinado.xml")) {
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
