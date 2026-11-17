package br.com.paymentslab.split.dominio.model;

import java.math.BigDecimal;

/**
 * Valor monetario em centavos.
 *
 * Nunca usa ponto flutuante. A conversao para decimal acontece apenas na borda,
 * quando o valor entra em uma mensagem ISO 20022.
 */
public record ValorMonetario(long centavos) {

    public ValorMonetario {
        if (centavos < 0) {
            throw new IllegalArgumentException("Valor monetario nao pode ser negativo");
        }
    }

    public static ValorMonetario zero() {
        return new ValorMonetario(0);
    }

    public static ValorMonetario deCentavos(long centavos) {
        return new ValorMonetario(centavos);
    }

    public BigDecimal emReais() {
        return BigDecimal.valueOf(centavos, 2);
    }

    public ValorMonetario mais(ValorMonetario outro) {
        return new ValorMonetario(Math.addExact(this.centavos, outro.centavos));
    }

    public ValorMonetario menos(ValorMonetario outro) {
        return new ValorMonetario(Math.subtractExact(this.centavos, outro.centavos));
    }

    public boolean maiorQue(ValorMonetario outro) {
        return this.centavos > outro.centavos;
    }
}
