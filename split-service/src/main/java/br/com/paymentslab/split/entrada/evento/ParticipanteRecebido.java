package br.com.paymentslab.split.entrada.evento;

/**
 * Participante como vem no evento de entrada.
 *
 * <p>E um modelo de borda, separado do dominio: se o produtor mudar o formato,
 * a mudanca para aqui.
 */
public record ParticipanteRecebido(
        String ispb,
        String nome,
        String documento,
        String agencia,
        String conta,
        String tipoConta) {
}
