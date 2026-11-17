package br.com.paymentslab.split.aplicacao.evento;

import java.time.Instant;

/**
 * Evento publicado quando a segregacao tributaria de um pagamento e calculada.
 *
 * Carrega o estado necessario para o proximo servico montar a mensagem pacs.008
 * sem precisar consultar o servico de origem. Isso e transferencia de estado por
 * evento, e evita acoplamento sincrono entre os servicos.
 */
public record SegregacaoCalculadaEvento(
        String endToEndId,
        long valorBrutoCentavos,
        long valorIbsCentavos,
        long valorCbsCentavos,
        long valorLiquidoCentavos,
        ParticipanteEvento pagador,
        ParticipanteEvento recebedor,
        String finalidade,
        String mensagem,
        Instant liquidadoEm) {
}
