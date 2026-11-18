package br.com.paymentslab.split.adaptadores.entrada.mensageria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.paymentslab.split.dominio.model.PagamentoLiquidado;
import br.com.paymentslab.split.dominio.model.Segregacao;
import br.com.paymentslab.split.dominio.model.ValorMonetario;
import br.com.paymentslab.split.dominio.porta.entrada.ProcessarPagamento;
import br.com.paymentslab.split.entrada.evento.PagamentoRecebido;
import br.com.paymentslab.split.entrada.evento.ParticipanteRecebido;
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
    void deve_recusar_evento_sem_end_to_end_id_sem_chamar_o_caso_de_uso() {
        AtomicInteger chamadas = new AtomicInteger();
        ProcessarPagamento caso = pagamento -> {
            chamadas.incrementAndGet();
            return segregacao(pagamento);
        };
        PagamentoRecebido invalido = evento(" ", 100);

        assertThrows(IllegalArgumentException.class,
                () -> new PagamentoConsumer(caso, metricas).consumir(invalido));

        assertEquals(0, chamadas.get(), "evento invalido nao pode chegar ao dominio");
        assertEquals(1.0, metricas.get("split.pagamentos")
                .tag("resultado", "recusado").counter().count());
    }

    @Test
    void deve_recusar_valor_zerado() {
        PagamentoRecebido invalido = evento("E9999901020260101120000000000001", 0);

        assertThrows(IllegalArgumentException.class,
                () -> new PagamentoConsumer(pagamento -> segregacao(pagamento), metricas)
                        .consumir(invalido));

        assertEquals(1.0, metricas.get("split.pagamentos")
                .tag("resultado", "recusado").counter().count());
    }

    @Test
    void deve_recusar_evento_sem_valor() {
        PagamentoRecebido semValor = new PagamentoRecebido(
                "E9999901020260101120000000000001", null, pagador(), recebedor(),
                "IPAY", "Campo livre", Instant.parse("2026-01-01T08:30:12Z"));

        assertThrows(IllegalArgumentException.class,
                () -> new PagamentoConsumer(pagamento -> segregacao(pagamento), metricas)
                        .consumir(semValor));
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

    private PagamentoRecebido evento(String endToEndId, long centavos) {
        return new PagamentoRecebido(
                endToEndId,
                new PagamentoRecebido.ValorRecebido(centavos),
                pagador(),
                recebedor(),
                "IPAY",
                "Campo livre",
                Instant.parse("2026-01-01T08:30:12Z"));
    }

    private PagamentoRecebido pagamento() {
        return evento("E9999901020260101120000000000001", 100);
    }

    private ParticipanteRecebido pagador() {
        return new ParticipanteRecebido(
                "00038166", "Fulano", "70000000000", "3000", "500000", "CACC");
    }

    private ParticipanteRecebido recebedor() {
        return new ParticipanteRecebido(
                "99999010", "Beltrano", "80000000000", "4000", "600000", "SVGS");
    }
}
