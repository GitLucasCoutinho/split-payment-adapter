package br.com.paymentslab.split.dominio.model;

import java.time.Instant;

/**
 * Pagamento liquidado que chega pela fila e precisa ser informado a Plataforma
 * Publica do Split Payment.
 *
 * O EndToEndId (End-to-End ID) e o identificador unico ponta a ponta da
 * transacao Pix e a chave de idempotencia do servico.
 */
public record PagamentoLiquidado(
        String endToEndId,
        ValorMonetario valor,
        Participante pagador,
        Participante recebedor,
        String finalidade,
        String mensagem,
        Instant liquidadoEm) {

    public PagamentoLiquidado {
        if (endToEndId == null || endToEndId.isBlank()) {
            throw new IllegalArgumentException("EndToEndId e obrigatorio");
        }
        if (valor == null || valor.centavos() <= 0) {
            throw new IllegalArgumentException("Valor deve ser maior que zero");
        }
        if (liquidadoEm == null) {
            throw new IllegalArgumentException("Data de liquidacao e obrigatoria");
        }
    }
}
