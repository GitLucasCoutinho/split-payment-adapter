package br.com.paymentslab.spienvio.entrada.evento;

import java.time.Instant;

/**
 * Evento de segregacao calculada, no formato que este servico consome.
 */
public record SegregacaoCalculadaRecebida(
        String endToEndId,
        long valorBrutoCentavos,
        long valorIbsCentavos,
        long valorCbsCentavos,
        long valorLiquidoCentavos,
        ParticipanteRecebido pagador,
        ParticipanteRecebido recebedor,
        String finalidade,
        String mensagem,
        Instant liquidadoEm) {
}
