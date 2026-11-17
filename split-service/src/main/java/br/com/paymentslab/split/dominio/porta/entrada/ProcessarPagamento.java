package br.com.paymentslab.split.dominio.porta.entrada;

import br.com.paymentslab.split.dominio.model.PagamentoLiquidado;
import br.com.paymentslab.split.dominio.model.Segregacao;

/**
 * Caso de uso de entrada: processar um pagamento liquidado.
 */
public interface ProcessarPagamento {

    Segregacao processar(PagamentoLiquidado pagamento);
}
