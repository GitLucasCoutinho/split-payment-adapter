package br.com.paymentslab.split.dominio.model;

/**
 * Participante de um pagamento: pagador ou recebedor.
 *
 * O ISPB (Identificador do Sistema de Pagamentos Brasileiro) e o codigo de oito
 * digitos que identifica a instituicao no SPI (Sistema de Pagamentos Instantaneos).
 */
public record Participante(
        String ispb,
        String nome,
        String documento,
        String agencia,
        String conta,
        String tipoConta) {

    public Participante {
        if (ispb == null || ispb.isBlank()) {
            throw new IllegalArgumentException("ISPB e obrigatorio");
        }
    }
}
