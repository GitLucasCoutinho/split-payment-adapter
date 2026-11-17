package br.com.paymentslab.spisimulador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Cada teste usa um EndToEndId proprio, porque o simulador guarda os
 * identificadores processados em memoria e o contexto do Spring e compartilhado
 * entre os metodos.
 *
 * <p>A mensagem valida vem de um fixture assinado. O simulador nao gera XML, ele
 * recebe, como o SPI real. O fixture tem o EndToEndId trocado por teste.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SimuladorControllerTest {

    private static final String END_TO_END_DO_FIXTURE = "E9999901020260101120000000000001";
    private static final String END_TO_END_ACEITE = "E9999901020260101120000000000101";
    private static final String END_TO_END_DUPLICADA = "E9999901020260101120000000000102";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deve_aceitar_mensagem_assinada_e_conforme() throws Exception {
        mockMvc.perform(post("/spi/mensagens")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(mensagemAssinada(END_TO_END_ACEITE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.protocolo").exists())
                .andExpect(jsonPath("$.endToEndId").value(END_TO_END_ACEITE));
    }

    @Test
    void deve_recusar_mensagem_sem_assinatura() throws Exception {
        mockMvc.perform(post("/spi/mensagens")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(exemploOficial()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("mensagem fora do contrato"));
    }

    @Test
    void deve_recusar_mensagem_duplicada() throws Exception {
        String xml = mensagemAssinada(END_TO_END_DUPLICADA);

        mockMvc.perform(post("/spi/mensagens")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(xml))
                .andExpect(status().isOk());

        mockMvc.perform(post("/spi/mensagens")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(xml))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("mensagem ja processada"));
    }

    private String mensagemAssinada(String endToEndId) throws Exception {
        return ler("/exemplo/pacs.008_assinado.xml").replace(END_TO_END_DO_FIXTURE, endToEndId);
    }

    private String exemploOficial() throws Exception {
        return ler("/exemplo/pacs.008_CONTA_1_msg.xml");
    }

    private String ler(String caminho) throws Exception {
        try (InputStream entrada = getClass().getResourceAsStream(caminho)) {
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
