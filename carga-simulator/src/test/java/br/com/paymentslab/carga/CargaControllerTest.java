package br.com.paymentslab.carga;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CargaControllerTest {

    @Test
    void deve_disparar_a_carga_e_devolver_o_resumo() {
        CargaController controller = new CargaController(
                new ExecutorCarga((chave, pagamento) -> { }));

        ExecutorCarga.ResultadoCarga resultado = controller.disparar(300, 1);

        assertTrue(resultado.publicados() > 0);
        assertEquals(300, resultado.taxaPorSegundo());
        assertEquals(1, resultado.segundos());
    }
}
