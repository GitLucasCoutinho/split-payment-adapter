# ADR-0005: Kafka entre o recebimento e a segregação

- Status: aceito
- Data: 2026-11-17

## Contexto

A segregação envolve três chamadas à PP (Plataforma Pública) mais o consumo do retorno. Se o endpoint de recebimento esperar tudo isso, qualquer lentidão externa vira lentidão no sistema interno, e uma indisponibilidade da PP vira indisponibilidade nossa.

## Decisão

Separar recebimento de processamento com um tópico no Kafka.

- O endpoint publica o evento e responde rápido.
- Um consumidor executa a segregação com retry limitado e DLT (Dead Letter Topic, tópico de mensagens que falharam).
- A chave da mensagem é o E2EID (End-to-End ID, identificador único ponta a ponta da transação Pix), para preservar ordem por transação.
- O contexto de trace é propagado por header, então o trace atravessa o Kafka.

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| Tudo síncrono | acopla disponibilidade e estoura o tempo de resposta |
| Fila em banco com scheduler | funciona, mas perde ordenação, particionamento e ferramental |
| Thread pool interna | não sobrevive a reinício e não tem DLT |

## Consequências

Positivas:

- O recebimento não depende da disponibilidade da PP.
- Retry, DLT e reprocessamento vêm de graça.
- O trace único de ponta a ponta vira demonstração de observabilidade.

Negativas:

- Existe consistência eventual entre receber e segregar.
- Adiciona Kafka ao ambiente local e ao compose.
- Exige idempotência no consumidor, porque entrega é ao menos uma vez.
