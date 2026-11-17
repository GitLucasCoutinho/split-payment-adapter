package br.com.paymentslab.split.dominio.servico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.paymentslab.split.dominio.model.PagamentoLiquidado;
import br.com.paymentslab.split.dominio.model.Participante;
import br.com.paymentslab.split.dominio.model.Segregacao;
import br.com.paymentslab.split.dominio.model.ValorMonetario;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CalculoSegregacaoPadraoTest {

    private final CalculoSegregacao calculo = new CalculoSegregacaoPadrao();

    @Test
    void deve_calcular_cbs_e_ibs_sobre_mil_reais() {
        Segregacao segregacao = calculo.calcular(pagamentoDe(100_000));

        assertEquals(100, segregacao.valorIbs().centavos(), "IBS de 0,1 por cento");
        assertEquals(900, segregacao.valorCbs().centavos(), "CBS de 0,9 por cento");
        assertEquals(99_000, segregacao.valorLiquido().centavos(), "liquido do estabelecimento");
    }

    @Test
    void deve_truncar_para_baixo_e_nunca_arredondar_para_cima() {
        // 111 centavos: CBS 0,999 e IBS 0,111. Ambos truncam para zero.
        Segregacao curta = calculo.calcular(pagamentoDe(111));
        assertEquals(0, curta.valorCbs().centavos());
        assertEquals(0, curta.valorIbs().centavos());
        assertEquals(111, curta.valorLiquido().centavos());

        // 112 centavos: CBS 1,008. Trunca para 1 centavo.
        Segregacao limite = calculo.calcular(pagamentoDe(112));
        assertEquals(1, limite.valorCbs().centavos());
        assertEquals(0, limite.valorIbs().centavos());
        assertEquals(111, limite.valorLiquido().centavos());
    }

    @Test
    void a_segregacao_deve_fechar_em_qualquer_valor() {
        for (long centavos = 1; centavos <= 2_000; centavos++) {
            Segregacao segregacao = calculo.calcular(pagamentoDe(centavos));
            long somaDasPartes = segregacao.valorLiquido().centavos()
                    + segregacao.valorIbs().centavos()
                    + segregacao.valorCbs().centavos();
            assertEquals(centavos, somaDasPartes,
                    "soma das partes deve igualar o bruto em " + centavos);
        }
    }

    @Test
    void os_tributos_nunca_devem_passar_do_valor_bruto() {
        for (long centavos = 1; centavos <= 2_000; centavos++) {
            Segregacao segregacao = calculo.calcular(pagamentoDe(centavos));
            assertTrue(segregacao.valorLiquido().centavos() >= 0);
            assertTrue(segregacao.totalTributos().centavos() <= centavos);
        }
    }

    private PagamentoLiquidado pagamentoDe(long centavos) {
        return new PagamentoLiquidado(
                "E9999901020260101120000000000001",
                ValorMonetario.deCentavos(centavos),
                new Participante("00038166", "Fulano", "70000000000", "0001", "500000", "CACC"),
                new Participante("99999010", "Beltrano", "80000000000", "0001", "600000", "SVGS"),
                "IPAY",
                "Teste",
                Instant.parse("2026-01-01T08:30:00Z"));
    }
}
