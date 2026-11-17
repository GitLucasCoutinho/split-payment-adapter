package br.com.paymentslab.spienvio.adaptadores.saida.pacs008;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.paymentslab.spienvio.pacs008.Assinador;
import br.com.paymentslab.spienvio.pacs008.AssinadorSimulado;
import br.com.paymentslab.spienvio.pacs008.Pacs008Codec;
import br.com.paymentslab.spienvio.entrada.evento.ParticipanteRecebido;
import br.com.paymentslab.spienvio.entrada.evento.SegregacaoCalculadaRecebida;
import br.gov.bcb.pi.pacs008.SPIEnvelopeMessage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class Pacs008MapperTest {

    private static final String ID_MENSAGEM = "M0003816612345678901234567890123";

    private final Pacs008Mapper mapper = new Pacs008Mapper();
    private final Pacs008Codec codec = new Pacs008Codec();
    private final Assinador assinador = new AssinadorSimulado();

    @Test
    void deve_gerar_mensagem_valida_contra_o_xsd() throws Exception {
        SPIEnvelopeMessage envelope = mapper.mapear(evento("Campo livre [0]", "3000"), ID_MENSAGEM);

        // escrever valida contra o XSD oficial. Se passar, a mensagem esta conforme.
        String xml = codec.escrever(envelope, assinador);

        assertTrue(xml.contains("<EndToEndId>E9999901020260101120000000000001</EndToEndId>"),
                "deve conter o EndToEndId");
        assertTrue(xml.contains("Ccy=\"BRL\">1000.00<"),
                "deve conter o valor bruto com duas casas decimais");
        assertTrue(xml.contains("<MmbId>00038166</MmbId>"),
                "deve conter o ISPB do pagador");
        assertTrue(xml.contains("<ds:Signature"), "deve conter a assinatura");

        Path destino = Path.of("target", "test-output", "spi-envio-service", "pacs.008_gerado.xml");
        Files.createDirectories(destino.getParent());
        Files.writeString(destino, xml, StandardCharsets.UTF_8);
    }

    @Test
    void deve_omitir_a_remessa_quando_a_mensagem_e_vazia() {
        String xml = codec.escrever(mapper.mapear(evento("   ", "3000"), ID_MENSAGEM), assinador);

        assertFalse(xml.contains("<RmtInf>"), "mensagem vazia nao deve gerar remessa");
    }

    @Test
    void deve_omitir_o_emissor_quando_a_agencia_e_ausente() {
        String xml = codec.escrever(mapper.mapear(evento("Campo livre", null), ID_MENSAGEM), assinador);

        // O recebedor tem agencia, o pagador nao. Deve sair apenas um Issr.
        int ocorrencias = xml.split("<Issr>", -1).length - 1;
        assertEquals(1, ocorrencias, "agencia ausente no pagador nao deve gerar emissor");
    }

    private SegregacaoCalculadaRecebida evento(String mensagem, String agenciaPagador) {
        return new SegregacaoCalculadaRecebida(
                "E9999901020260101120000000000001",
                100_000,
                100,
                900,
                99_000,
                new ParticipanteRecebido("00038166", "Fulano da Silva", "70000000000", agenciaPagador, "500000", "CACC"),
                new ParticipanteRecebido("99999010", "Beltrano Souza", "80000000000", "4000", "600000", "SVGS"),
                "IPAY",
                mensagem,
                Instant.parse("2026-01-01T08:30:12Z"));
    }
}
