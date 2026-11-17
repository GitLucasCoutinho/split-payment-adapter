package br.com.paymentslab.spienvio.entrada.evento;

/**
 * Participante no evento consumido.
 *
 * E o modelo do lado consumidor, independente do modelo do servico que publica.
 * Essa independencia e proposital: se o produtor mudar o formato, a quebra fica
 * localizada aqui, e nao espalhada pelo servico.
 */
public record ParticipanteRecebido(
        String ispb,
        String nome,
        String documento,
        String agencia,
        String conta,
        String tipoConta) {
}
