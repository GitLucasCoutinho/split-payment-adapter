package br.com.paymentslab.split.dominio.servico;

import br.com.paymentslab.split.dominio.model.PagamentoLiquidado;
import br.com.paymentslab.split.dominio.model.Segregacao;

/**
 * Porta do calculo de segregacao tributaria.
 *
 * Fica como interface porque a regra de proporcionalidade ainda nao esta
 * fechada (duvida D1 da spec). Trocar a regra nao deve tocar no caso de uso.
 */
public interface CalculoSegregacao {

    Segregacao calcular(PagamentoLiquidado pagamento);
}
