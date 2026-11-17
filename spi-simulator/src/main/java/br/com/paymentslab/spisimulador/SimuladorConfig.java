package br.com.paymentslab.spisimulador;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimuladorConfig {

    @Bean
    public ValidadorPacs008 validadorPacs008() {
        return new ValidadorPacs008();
    }
}
