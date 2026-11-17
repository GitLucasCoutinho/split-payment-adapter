package br.com.paymentslab.split.dominio.servico;

import br.com.paymentslab.split.dominio.model.PagamentoLiquidado;
import br.com.paymentslab.split.dominio.model.Segregacao;
import br.com.paymentslab.split.dominio.model.ValorMonetario;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculo de segregacao com as aliquotas de transicao de 2026.
 *
 * Premissa documentada: CBS de 0,9% e IBS de 0,1%, percentuais de teste do
 * periodo de transicao da reforma tributaria. O calculo e proporcional ao valor
 * bruto e sempre truncado para baixo.
 *
 * Esta premissa esta registrada como duvida aberta (D1) na spec. Quando a regra
 * definitiva for publicada, troca-se esta implementacao.
 */
public final class CalculoSegregacaoPadrao implements CalculoSegregacao {

    private static final BigDecimal ALIQUOTA_CBS = new BigDecimal("0.009");
    private static final BigDecimal ALIQUOTA_IBS = new BigDecimal("0.001");

    @Override
    public Segregacao calcular(PagamentoLiquidado pagamento) {
        long bruto = pagamento.valor().centavos();
        long cbs = truncar(bruto, ALIQUOTA_CBS);
        long ibs = truncar(bruto, ALIQUOTA_IBS);
        long liquido = bruto - cbs - ibs;

        return new Segregacao(
                pagamento.endToEndId(),
                pagamento.valor(),
                ValorMonetario.deCentavos(ibs),
                ValorMonetario.deCentavos(cbs),
                ValorMonetario.deCentavos(liquido));
    }

    private long truncar(long centavos, BigDecimal aliquota) {
        return BigDecimal.valueOf(centavos)
                .multiply(aliquota)
                .setScale(0, RoundingMode.DOWN)
                .longValueExact();
    }
}
