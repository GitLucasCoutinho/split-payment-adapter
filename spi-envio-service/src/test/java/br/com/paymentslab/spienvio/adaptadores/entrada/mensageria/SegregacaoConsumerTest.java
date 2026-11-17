package br.com.paymentslab.spienvio.adaptadores.entrada.mensageria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.paymentslab.spienvio.adaptadores.saida.pacs008.Pacs008Mapper;
import br.com.paymentslab.spienvio.dominio.porta.saida.EnvioSpi;
import br.com.paymentslab.spienvio.dominio.porta.saida.Protocolo;
import br.com.paymentslab.spienvio.entrada.evento.ParticipanteRecebido;
import br.com.paymentslab.spienvio.entrada.evento.SegregacaoCalculadaRecebida;
import br.com.paymentslab.spienvio.pacs008.AssinadorSimulado;
import br.com.paymentslab.spienvio.pacs008.Pacs008Codec;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SegregacaoConsumerTest {

    private final SimpleMeterRegistry metricas = new SimpleMeterRegistry();

    @Test
    void deve_montar_assinar_validar_e_enviar() {
        List<String> enviados = new ArrayList<>();
        EnvioSpi envio = xml -> {
            enviados.add(xml);
            return new Protocolo("SIM-1", Instant.now());
        };

        SegregacaoConsumer consumer = new SegregacaoConsumer(
                new Pacs008Mapper(),
                new Pacs008Codec(),
                new AssinadorSimulado(),
                envio,
                metricas);

        consumer.consumir(evento());

        assertEquals(1, enviados.size());
        String xml = enviados.get(0);
        assertTrue(xml.contains("<ds:Signature"), "a mensagem deve estar assinada");
        assertTrue(xml.contains("E9999901020260101120000000000001"),
                "a mensagem deve carregar o EndToEndId");
    }

    @Test
    void deve_contar_envio_bem_sucedido_e_medir_o_tempo() {
        SegregacaoConsumer consumer = new SegregacaoConsumer(
                new Pacs008Mapper(),
                new Pacs008Codec(),
                new AssinadorSimulado(),
                xml -> new Protocolo("SIM-1", Instant.now()),
                metricas);

        consumer.consumir(evento());

        assertEquals(1.0, metricas.get("pacs008.envios")
                .tag("resultado", "enviado").counter().count());
        assertEquals(1, metricas.get("pacs008.envio").timer().count());
    }

    @Test
    void deve_contar_falha_quando_o_envio_quebra() {
        SegregacaoConsumer consumer = new SegregacaoConsumer(
                new Pacs008Mapper(),
                new Pacs008Codec(),
                new AssinadorSimulado(),
                xml -> {
                    throw new IllegalStateException("SPI fora do ar");
                },
                metricas);

        assertThrows(IllegalStateException.class, () -> consumer.consumir(evento()));

        assertEquals(1.0, metricas.get("pacs008.envios")
                .tag("resultado", "falha").counter().count());
    }

    private SegregacaoCalculadaRecebida evento() {
        return new SegregacaoCalculadaRecebida(
                "E9999901020260101120000000000001",
                100_000,
                100,
                900,
                99_000,
                new ParticipanteRecebido("00038166", "Fulano da Silva", "70000000000", "3000", "500000", "CACC"),
                new ParticipanteRecebido("99999010", "Beltrano Souza", "80000000000", "4000", "600000", "SVGS"),
                "IPAY",
                "Campo livre [0]",
                Instant.parse("2026-01-01T08:30:12Z"));
    }
}
