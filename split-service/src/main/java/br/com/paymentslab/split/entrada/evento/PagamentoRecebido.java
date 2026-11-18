package br.com.paymentslab.split.entrada.evento;

import java.time.Instant;

/**
 * Pagamento liquidado como chega no topico.
 *
 * <p>Modelo de borda, sem regra de negocio. A validacao acontece no mapeamento
 * para o dominio, onde a recusa tem significado e vira metrica.
 *
 * <p>A forma dos campos espelha o JSON publicado. Mudar aqui sem mudar o
 * produtor quebra o contrato.
 */
public record PagamentoRecebido(
        String endToEndId,
        ValorRecebido valor,
        ParticipanteRecebido pagador,
        ParticipanteRecebido recebedor,
        String finalidade,
        String mensagem,
        Instant liquidadoEm) {

    /** Valor como vem no evento. */
    public record ValorRecebido(long centavos) {
    }
}
