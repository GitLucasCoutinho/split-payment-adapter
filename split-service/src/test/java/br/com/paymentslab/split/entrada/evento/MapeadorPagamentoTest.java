package br.com.paymentslab.split.entrada.evento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.paymentslab.split.dominio.model.PagamentoLiquidado;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class MapeadorPagamentoTest {

    private final MapeadorPagamento mapeador = new MapeadorPagamento();

    @Test
    void deve_converter_o_evento_no_modelo_de_dominio() {
        PagamentoRecebido recebido = evento(250_000);

        PagamentoLiquidado pagamento = mapeador.paraDominio(recebido);

        assertEquals(recebido.endToEndId(), pagamento.endToEndId());
        assertEquals(250_000, pagamento.valor().centavos());
        assertEquals("00038166", pagamento.pagador().ispb());
        assertEquals("99999010", pagamento.recebedor().ispb());
    }

    @Test
    void deve_recusar_evento_ausente() {
        assertThrows(IllegalArgumentException.class, () -> mapeador.paraDominio(null));
    }

    @Test
    void deve_recusar_participante_ausente_com_motivo() {
        PagamentoRecebido semPagador = new PagamentoRecebido(
                "E9999901020260101120000000000001",
                new PagamentoRecebido.ValorRecebido(100),
                null,
                recebedor(),
                "IPAY",
                "Campo livre",
                Instant.parse("2026-01-01T08:30:12Z"));

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> mapeador.paraDominio(semPagador));

        assertTrue(erro.getMessage().contains("recusado"), erro.getMessage());
    }

    @Test
    void deve_recusar_valor_ausente() {
        PagamentoRecebido semValor = new PagamentoRecebido(
                "E9999901020260101120000000000001", null, pagador(), recebedor(),
                "IPAY", "Campo livre", Instant.parse("2026-01-01T08:30:12Z"));

        assertThrows(IllegalArgumentException.class, () -> mapeador.paraDominio(semValor));
    }

    @Test
    void deve_recusar_valor_invalido_com_motivo() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> mapeador.paraDominio(evento(0)));

        assertTrue(erro.getMessage().contains("recusado"), erro.getMessage());
    }

    private PagamentoRecebido evento(long centavos) {
        return new PagamentoRecebido(
                "E9999901020260101120000000000001",
                new PagamentoRecebido.ValorRecebido(centavos),
                pagador(),
                recebedor(),
                "IPAY",
                "Campo livre",
                Instant.parse("2026-01-01T08:30:12Z"));
    }

    private ParticipanteRecebido pagador() {
        return new ParticipanteRecebido("00038166", "Fulano", "70000000000", "3000", "500000", "CACC");
    }

    private ParticipanteRecebido recebedor() {
        return new ParticipanteRecebido("99999010", "Beltrano", "80000000000", "4000", "600000", "SVGS");
    }
}
