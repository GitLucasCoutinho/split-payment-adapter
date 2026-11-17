package br.com.paymentslab.carga;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Executa a carga: publica pagamentos numa taxa por segundo, durante um tempo.
 *
 * <p>Serve para exercitar o fluxo com volume real e ver metrica, lag de consumer
 * e log no Kibana se movendo. A taxa e o tempo sao limitados para nao derrubar a
 * maquina de quem esta demonstrando.
 */
@Component
public class ExecutorCarga {

    private static final Logger log = LoggerFactory.getLogger(ExecutorCarga.class);

    private final PublicadorPagamento publicador;
    private final GeradorPagamento gerador = new GeradorPagamento();

    public ExecutorCarga(PublicadorPagamento publicador) {
        this.publicador = publicador;
    }

    public ResultadoCarga executar(int taxaPorSegundo, int segundos) {
        Parametros parametros = Parametros.de(taxaPorSegundo, segundos);
        log.info("Carga iniciada: taxa={}/s duracao={}s", parametros.taxa(), parametros.segundos());

        long intervaloMs = Math.max(0L, 1_000L / parametros.taxa());
        long inicio = System.currentTimeMillis();
        long limite = inicio + parametros.segundos() * 1_000L;
        int publicados = 0;

        while (System.currentTimeMillis() < limite && !Thread.currentThread().isInterrupted()) {
            Map<String, Object> pagamento = gerador.proximoPagamento();
            publicador.publicar((String) pagamento.get("endToEndId"), pagamento);
            publicados++;
            if (intervaloMs > 0 && !pausar(intervaloMs)) {
                break;
            }
        }

        long duracaoMs = System.currentTimeMillis() - inicio;
        log.info("Carga concluida: publicados={} em {} ms", publicados, duracaoMs);
        return new ResultadoCarga(publicados, duracaoMs, parametros.taxa(), parametros.segundos());
    }

    private boolean pausar(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /** Resumo devolvido a quem disparou a carga. */
    public record ResultadoCarga(int publicados, long duracaoMs, int taxaPorSegundo, int segundos) {
    }

    /**
     * Limites da carga.
     *
     * <p>Fica separado para ser testavel sem executar a carga inteira.
     */
    public record Parametros(int taxa, int segundos) {

        public static final int TAXA_MAXIMA = 5_000;
        public static final int DURACAO_MAXIMA_SEGUNDOS = 300;

        public static Parametros de(int taxa, int segundos) {
            int taxaLimitada = Math.max(1, Math.min(taxa, TAXA_MAXIMA));
            int duracaoLimitada = Math.max(1, Math.min(segundos, DURACAO_MAXIMA_SEGUNDOS));
            return new Parametros(taxaLimitada, duracaoLimitada);
        }
    }
}
