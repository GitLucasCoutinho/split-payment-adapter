# ADR-0011: mensagem que falha vai para o tópico de erro

- Status: aceito
- Data: 2026-11-17

## Contexto

Um consumidor Kafka avança o offset depois de processar. Se o processamento lança exceção e não existe tratamento, a mensagem é reprocessada indefinidamente e a partição fica travada: nada atrás dela avança.

No `spi-envio-service` o risco é concreto. O envio é uma chamada HTTP, e o simulador pode estar fora. Sem tratamento, uma mensagem com o SPI indisponível pararia o fluxo inteiro.

## Decisão

Todo consumidor tem um `DefaultErrorHandler` com `DeadLetterPublishingRecoverer`.

| Parâmetro | Valor |
|---|---|
| Tentativas extras | 3 |
| Intervalo entre tentativas | 1 segundo no `split-service`, 2 segundos no `spi-envio-service` |
| Destino após esgotar | `<tópico>-dlt` |

A mensagem morta preserva chave e cabeçalhos, inclusive o identificador de correlação, então é possível rastrear a falha no Kibana pelo mesmo E2EID.

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| Deixar sem tratamento | a partição trava e a falha se propaga para todo o fluxo |
| Apenas logar e seguir | a mensagem é perdida sem rastro |
| Retry indefinido | transforma indisponibilidade temporária em bloqueio permanente |

## Consequências

Positivas:

- Uma mensagem ruim não bloqueia as seguintes.
- O erro fica auditável, com payload e correlação.
- Indisponibilidade do SPI vira atraso, não perda.

Negativas:

- A DLQ precisa de alguém olhando. Sem monitoração, vira depósito.
- A métrica de falha conta **tentativas**, não mensagens. Quatro tentativas viram quatro incrementos no contador.
- Reprocessar a DLQ hoje é manual: copiar a mensagem de volta para o tópico original.
