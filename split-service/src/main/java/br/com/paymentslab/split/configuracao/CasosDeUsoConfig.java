package br.com.paymentslab.split.configuracao;

import br.com.paymentslab.split.aplicacao.usecase.implementacao.ProcessarPagamentoImpl;
import br.com.paymentslab.split.dominio.porta.entrada.ProcessarPagamento;
import br.com.paymentslab.split.dominio.porta.saida.PublicadorEvento;
import br.com.paymentslab.split.dominio.servico.CalculoSegregacao;
import br.com.paymentslab.split.dominio.servico.CalculoSegregacaoPadrao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Liga os casos de uso, que nao conhecem Spring, ao contexto da aplicacao.
 */
@Configuration
public class CasosDeUsoConfig {

    @Bean
    public CalculoSegregacao calculoSegregacao() {
        return new CalculoSegregacaoPadrao();
    }

    @Bean
    public ProcessarPagamento processarPagamento(
            CalculoSegregacao calculoSegregacao,
            PublicadorEvento publicadorEvento) {
        return new ProcessarPagamentoImpl(calculoSegregacao, publicadorEvento);
    }
}
