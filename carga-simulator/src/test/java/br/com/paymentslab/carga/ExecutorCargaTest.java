package br.com.paymentslab.carga;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExecutorCargaTest {

    @Test
    void deve_publicar_na_taxa_pedida_durante_o_tempo_pedido() {
        List<String> chaves = new ArrayList<>();
        ExecutorCarga executar = new ExecutorCarga((chave, pagamento) -> chaves.add(chave));

        ExecutorCarga.ResultadoCarga resultado = executar.executar(500, 1);

        assertTrue(resultado.publicados() > 0, "deve publicar alguma coisa");
        assertEquals(resultado.publicados(), chaves.size(), "cada publicacao vira uma chave");
        assertTrue(resultado.duracaoMs() >= 900, "deve respeitar a duracao pedida");
    }

    @Test
    void deve_usar_uma_chave_unica_por_pagamento() {
        List<String> chaves = new ArrayList<>();
        ExecutorCarga executar = new ExecutorCarga((chave, pagamento) -> chaves.add(chave));

        executar.executar(500, 1);

        assertEquals(chaves.size(), chaves.stream().distinct().count(),
                "chave repetida faria o simulador recusar como duplicata");
    }

    @Test
    void deve_limitar_taxa_e_duracao_absurdas() {
        ExecutorCarga.Parametros parametros = ExecutorCarga.Parametros.de(999_999, 9_999);

        assertEquals(5_000, parametros.taxa());
        assertEquals(300, parametros.segundos());
    }

    @Test
    void deve_tratar_taxa_ou_duracao_invalida_como_minimo() {
        ExecutorCarga.Parametros parametros = ExecutorCarga.Parametros.de(0, 0);

        assertEquals(1, parametros.taxa());
        assertEquals(1, parametros.segundos());
    }

    @Test
    void deve_parar_quando_a_thread_e_interrompida() {
        ExecutorCarga executar = new ExecutorCarga(
                (chave, pagamento) -> Thread.currentThread().interrupt());

        ExecutorCarga.ResultadoCarga resultado = executar.executar(1_000, 30);

        assertTrue(resultado.duracaoMs() < 5_000,
                "a carga deve parar assim que a thread for interrompida");
        Thread.interrupted();
    }
}
