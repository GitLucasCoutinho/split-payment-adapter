package br.com.paymentslab.split.entrada.evento;

import br.com.paymentslab.split.dominio.model.PagamentoLiquidado;
import br.com.paymentslab.split.dominio.model.Participante;
import br.com.paymentslab.split.dominio.model.ValorMonetario;

/**
 * Converte o evento de borda no modelo de dominio.
 *
 * <p>Aqui mora a diferenca entre evento e dominio. O evento pode vir torto, e
 * mensagem torta precisa ser recusada com motivo, nao estourar dentro do
 * desserializador.
 */
public final class MapeadorPagamento {

    public PagamentoLiquidado paraDominio(PagamentoRecebido recebido) {
        if (recebido == null) {
            throw new IllegalArgumentException("Evento de pagamento ausente");
        }
        try {
            return new PagamentoLiquidado(
                    recebido.endToEndId(),
                    valor(recebido),
                    converter(recebido.pagador()),
                    converter(recebido.recebedor()),
                    recebido.finalidade(),
                    recebido.mensagem(),
                    recebido.liquidadoEm());
        } catch (IllegalArgumentException erro) {
            throw new IllegalArgumentException(
                    "Evento de pagamento recusado: " + erro.getMessage(), erro);
        }
    }

    private ValorMonetario valor(PagamentoRecebido recebido) {
        if (recebido.valor() == null) {
            throw new IllegalArgumentException("Valor ausente");
        }
        return ValorMonetario.deCentavos(recebido.valor().centavos());
    }

    private Participante converter(ParticipanteRecebido recebido) {
        if (recebido == null) {
            throw new IllegalArgumentException("Participante ausente");
        }
        return new Participante(
                recebido.ispb(),
                recebido.nome(),
                recebido.documento(),
                recebido.agencia(),
                recebido.conta(),
                recebido.tipoConta());
    }
}
