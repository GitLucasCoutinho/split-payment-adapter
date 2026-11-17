package br.com.paymentslab.spienvio.pacs008;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.gov.bcb.pi.pacs008.SPIEnvelopeMessage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class Pacs008CodecTest {

    private final Pacs008Codec codec = new Pacs008Codec();

    @Test
    void deveLerEscreverSemAlterarOConteudo() throws Exception {
        String original = lerExemplo();

        SPIEnvelopeMessage envelope =
                codec.ler(new ByteArrayInputStream(original.getBytes(StandardCharsets.UTF_8)));
        assertNotNull(envelope.getAppHdr(), "cabecalho deve ser lido");
        assertNotNull(envelope.getDocument(), "documento deve ser lido");

        String gerado = codec.escreverSemValidar(envelope);
        gravar(gerado, "pacs.008_CONTA_1_sem_assinatura.xml");

        assertEquals(normalizar(original), normalizar(gerado),
                "o XML gerado deve ser igual ao exemplo, ignorando formatacao");
    }

    @Test
    void oExemploNaoEValidoSemAssinatura() throws Exception {
        String original = lerExemplo();

        Exception erro = assertThrows(IllegalStateException.class, () -> codec.validar(original));

        assertTrue(erro.getMessage().contains("Sgntr"),
                "a falha deve apontar o elemento Sgntr, que exige assinatura digital");
    }

    @Test
    void deveAssinarEValidarContraOXsd() throws Exception {
        String original = lerExemplo();
        SPIEnvelopeMessage envelope =
                codec.ler(new ByteArrayInputStream(original.getBytes(StandardCharsets.UTF_8)));

        String assinado = codec.escrever(envelope, new AssinadorSimulado());
        gravar(assinado, "pacs.008_CONTA_1_assinado.xml");

        assertTrue(assinado.contains("ds:Signature"), "a mensagem deve conter a assinatura");
        assertTrue(assinado.contains("<ds:SignatureValue>"), "a assinatura deve ter valor");
    }

    @Test
    void deveRecusarXmlForaDoSchema() {
        String invalido = "<Envelope xmlns=\"https://www.bcb.gov.br/pi/pacs.008/1.15\"><AppHdr/></Envelope>";
        assertThrows(IllegalStateException.class, () -> codec.validar(invalido));
    }

    private void gravar(String xml, String nome) throws Exception {
        Path destino = Path.of("target", "test-output", "pacs008", nome);
        Files.createDirectories(destino.getParent());
        Files.writeString(destino, xml, StandardCharsets.UTF_8);
    }

    private String lerExemplo() throws Exception {
        try (InputStream entrada = getClass().getResourceAsStream("/exemplo/pacs.008_CONTA_1_msg.xml")) {
            assertNotNull(entrada, "exemplo deve estar no classpath");
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Normaliza para comparar conteudo, nao bytes.
     *
     * Remove a declaracao XML, o espaco entre elementos e as declaracoes de
     * namespace com prefixo que nao sao usadas. O namespace padrao permanece,
     * porque ele muda o significado do documento.
     */
    private static String normalizar(String xml) {
        return xml.replaceFirst("<\\?xml[^>]*\\?>", "")
                .replaceAll("\\s+xmlns:[A-Za-z0-9._-]+=\"[^\"]*\"", "")
                .replaceAll(">\\s+<", "><")
                .trim();
    }
}
