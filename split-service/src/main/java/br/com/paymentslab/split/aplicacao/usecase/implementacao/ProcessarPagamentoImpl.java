package br.com.paymentslab.split.aplicacao.usecase.implementacao;

import br.com.paymentslab.split.aplicacao.evento.ParticipanteEvento;
import br.com.paymentslab.split.aplicacao.evento.SegregacaoCalculadaEvento;
import br.com.paymentslab.split.dominio.model.PagamentoLiquidado;
import br.com.paymentslab.split.dominio.model.Participante;
import br.com.paymentslab.split.dominio.model.Segregacao;
import br.com.paymentslab.split.dominio.porta.entrada.ProcessarPagamento;
import br.com.paymentslab.split.dominio.porta.saida.PublicadorEvento;
import br.com.paymentslab.split.dominio.servico.CalculoSegregacao;

/**
 * Orquestra o processamento: calcula a segregacao e publica o resultado.
 *
 * Sem anotacao de framework. A ligacao com o Spring fica em configuracao.
 */
public final class ProcessarPagamentoImpl implements ProcessarPagamento {

    public static final String TOPICO_SEGREGACAO = "segregacao.calculada.v1";

    private final CalculoSegregacao calculo;
    private final PublicadorEvento publicador;

    public ProcessarPagamentoImpl(CalculoSegregacao calculo, PublicadorEvento publicador) {
        this.calculo = calculo;
        this.publicador = publicador;
    }

    @Override
    public Segregacao processar(PagamentoLiquidado pagamento) {
        Segregacao segregacao = calculo.calcular(pagamento);

        SegregacaoCalculadaEvento evento = new SegregacaoCalculadaEvento(
                segregacao.endToEndId(),
                segregacao.valorBruto().centavos(),
                segregacao.valorIbs().centavos(),
                segregacao.valorCbs().centavos(),
                segregacao.valorLiquido().centavos(),
                converter(pagamento.pagador()),
                converter(pagamento.recebedor()),
                pagamento.finalidade(),
                pagamento.mensagem(),
                pagamento.liquidadoEm());

        publicador.publicar(TOPICO_SEGREGACAO, pagamento.endToEndId(), evento);
        return segregacao;
    }

    private ParticipanteEvento converter(Participante participante) {
        return new ParticipanteEvento(
                participante.ispb(),
                participante.nome(),
                participante.documento(),
                participante.agencia(),
                participante.conta(),
                participante.tipoConta());
    }
}
