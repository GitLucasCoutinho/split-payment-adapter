package br.com.paymentslab.split.aplicacao.usecase.implementacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.paymentslab.split.aplicacao.evento.SegregacaoCalculadaEvento;
import br.com.paymentslab.split.dominio.model.PagamentoLiquidado;
import br.com.paymentslab.split.dominio.model.Participante;
import br.com.paymentslab.split.dominio.model.Segregacao;
import br.com.paymentslab.split.dominio.model.ValorMonetario;
import br.com.paymentslab.split.dominio.porta.saida.PublicadorEvento;
import br.com.paymentslab.split.dominio.servico.CalculoSegregacaoPadrao;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProcessarPagamentoImplTest {

    @Test
    void deve_publicar_evento_com_os_dados_do_pagamento() {
        List<String> topicos = new ArrayList<>();
        List<Object> eventos = new ArrayList<>();
        PublicadorEvento publicador = (topico, chave, evento) -> {
            topicos.add(topico);
            eventos.add(evento);
        };

        ProcessarPagamentoImpl caso = new ProcessarPagamentoImpl(new CalculoSegregacaoPadrao(), publicador);
        Segregacao segregacao = caso.processar(pagamento());

        assertEquals(100, segregacao.valorIbs().centavos());
        assertEquals(900, segregacao.valorCbs().centavos());
        assertEquals(1, eventos.size());
        assertEquals("segregacao.calculada.v1", topicos.get(0));

        SegregacaoCalculadaEvento evento = (SegregacaoCalculadaEvento) eventos.get(0);
        assertEquals("E9999901020260101120000000000001", evento.endToEndId());
        assertEquals(100_000, evento.valorBrutoCentavos());
        assertEquals(99_000, evento.valorLiquidoCentavos());
        assertEquals("00038166", evento.pagador().ispb());
        assertEquals("99999010", evento.recebedor().ispb());
        assertTrue(evento.liquidadoEm().isBefore(Instant.now()));
    }

    private PagamentoLiquidado pagamento() {
        return new PagamentoLiquidado(
                "E9999901020260101120000000000001",
                ValorMonetario.deCentavos(100_000),
                new Participante("00038166", "Fulano", "70000000000", "3000", "500000", "CACC"),
                new Participante("99999010", "Beltrano", "80000000000", "4000", "600000", "SVGS"),
                "IPAY",
                "Campo livre",
                Instant.parse("2026-01-01T08:30:12Z"));
    }
}
