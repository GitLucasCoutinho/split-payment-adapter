package br.com.paymentslab.split.adaptadores.entrada.mensageria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.paymentslab.split.dominio.model.PagamentoLiquidado;
import br.com.paymentslab.split.dominio.model.Participante;
import br.com.paymentslab.split.dominio.model.Segregacao;
import br.com.paymentslab.split.dominio.model.ValorMonetario;
import br.com.paymentslab.split.dominio.porta.entrada.ProcessarPagamento;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class PagamentoConsumerTest {

    private final SimpleMeterRegistry metricas = new SimpleMeterRegistry();

    @Test
    void deve_encaminhar_o_pagamento_ao_caso_de_uso() {
        AtomicInteger chamadas = new AtomicInteger();
        ProcessarPagamento caso = pagamento -> {
            chamadas.incrementAndGet();
            return segregacao(pagamento);
        };

        new PagamentoConsumer(caso, metricas).consumir(pagamento());

        assertEquals(1, chamadas.get());
    }

    @Test
    void deve_contar_pagamento_calculado() {
        new PagamentoConsumer(pagamento -> segregacao(pagamento), metricas).consumir(pagamento());

        assertEquals(1.0, metricas.get("split.pagamentos")
                .tag("resultado", "calculado").counter().count());
        assertEquals(1, metricas.get("split.calculo").timer().count(),
                "o tempo de calculo precisa ser medido");
    }

    @Test
    void deve_contar_falha_e_deixar_o_erro_subir() {
        PagamentoConsumer consumer = new PagamentoConsumer(pagamento -> {
            throw new IllegalStateException("calculo quebrou");
        }, metricas);

        assertThrows(IllegalStateException.class, () -> consumer.consumir(pagamento()));

        assertEquals(1.0, metricas.get("split.pagamentos")
                .tag("resultado", "falha").counter().count());
    }

    private Segregacao segregacao(PagamentoLiquidado pagamento) {
        return new Segregacao(
                pagamento.endToEndId(),
                ValorMonetario.deCentavos(100),
                ValorMonetario.deCentavos(0),
                ValorMonetario.deCentavos(1),
                ValorMonetario.deCentavos(99));
    }

    private PagamentoLiquidado pagamento() {
        return new PagamentoLiquidado(
                "E9999901020260101120000000000001",
                ValorMonetario.deCentavos(100),
                new Participante("00038166", "Fulano", "70000000000", "3000", "500000", "CACC"),
                new Participante("99999010", "Beltrano", "80000000000", "4000", "600000", "SVGS"),
                "IPAY",
                "Campo livre",
                Instant.parse("2026-01-01T08:30:12Z"));
    }
}
