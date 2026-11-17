package br.com.paymentslab.split.aplicacao.evento;

/**
 * Participante no evento publicado entre servicos.
 *
 * E um DTO de aplicacao, nao o modelo de dominio. Existe para que o contrato
 * publicado nao dependa da estrutura interna do servico.
 */
public record ParticipanteEvento(
        String ispb,
        String nome,
        String documento,
        String agencia,
        String conta,
        String tipoConta) {
}
