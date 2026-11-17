package br.com.paymentslab.split.dominio.model;

/**
 * Resultado do calculo de segregacao tributaria de um pagamento.
 *
 * Invariante: bruto = liquido + IBS + CBS, sempre. O calculo trunca para baixo,
 * nunca arredonda para cima, porque reter mais tributo do que o devido e erro
 * fiscal.
 */
public record Segregacao(
        String endToEndId,
        ValorMonetario valorBruto,
        ValorMonetario valorIbs,
        ValorMonetario valorCbs,
        ValorMonetario valorLiquido) {

    public Segregacao {
        ValorMonetario total = valorLiquido.mais(valorIbs).mais(valorCbs);
        if (total.centavos() != valorBruto.centavos()) {
            throw new IllegalArgumentException(
                    "Segregacao nao fecha: bruto " + valorBruto.centavos()
                            + ", soma das partes " + total.centavos());
        }
    }

    public ValorMonetario totalTributos() {
        return valorIbs.mais(valorCbs);
    }
}
