package br.com.paymentslab.carga;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dispara a carga por HTTP.
 *
 * <p>Exemplo: {@code POST /carga?taxa=200&segundos=20} publica cerca de 4.000
 * pagamentos liquidados. A chamada so responde ao terminar, com o resumo.
 */
@RestController
@RequestMapping("/carga")
public class CargaController {

    private final ExecutorCarga executor;

    public CargaController(ExecutorCarga executor) {
        this.executor = executor;
    }

    @PostMapping
    public ExecutorCarga.ResultadoCarga disparar(
            @RequestParam(defaultValue = "50") int taxa,
            @RequestParam(defaultValue = "10") int segundos) {
        return executor.executar(taxa, segundos);
    }
}
