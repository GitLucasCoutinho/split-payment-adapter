package br.com.paymentslab.spienvio.configuracao;

import br.com.paymentslab.spienvio.pacs008.Assinador;
import br.com.paymentslab.spienvio.pacs008.AssinadorSimulado;
import br.com.paymentslab.spienvio.pacs008.Pacs008Codec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declara o codec e o assinador como beans.
 *
 * O assinador simulado e o padrao. Para usar certificado real, basta declarar
 * outro bean Assinador, sem tocar no consumidor.
 */
@Configuration
public class ContratoConfig {

    @Bean
    public Pacs008Codec pacs008Codec() {
        return new Pacs008Codec();
    }

    @Bean
    public Assinador assinador() {
        return new AssinadorSimulado();
    }
}
