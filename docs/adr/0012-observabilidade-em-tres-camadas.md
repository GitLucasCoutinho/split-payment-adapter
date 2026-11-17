# ADR-0012: observabilidade em três camadas

- Status: aceito
- Data: 2026-11-17

## Contexto

Um sistema de pagamento precisa responder três perguntas diferentes:

1. Quantas transações passaram e quantas falharam?
2. Por que esta transação específica falhou?
3. O que aconteceu no serviço naquele instante?

Uma ferramenta só não responde às três. Métrica não guarda identidade, log não guarda série temporal, e trace sozinho não dá o contexto de negócio.

## Decisão

Três camadas, cada uma com a sua ferramenta.

| Camada | Ferramenta | Responde |
|---|---|---|
| Métrica | Micrometer com registro Prometheus | quantas e com que latência |
| Log estruturado | JSON no Elasticsearch, lido no Kibana | o que aconteceu, linha por linha |
| Correlação | MDC com o E2EID, propagado do Kafka ao HTTP | por que esta transação |

As métricas ficam no adaptador, nunca no domínio. O núcleo não importa biblioteca de observabilidade.

### Métricas de negócio

| Métrica | Serviço | Tipo |
|---|---|---|
| `split_pagamentos_total{resultado}` | `split-service` | contador |
| `split_calculo_seconds` | `split-service` | timer |
| `pacs008_envios_total{resultado}` | `spi-envio-service` | contador |
| `pacs008_envio_seconds` | `spi-envio-service` | timer |
| `simulador_mensagens_total{resultado}` | `spi-simulator` | contador |
| `simulador_validacao_seconds` | `spi-simulator` | timer |

### Log e Kibana

O Logback escreve duas saídas: console para leitura humana e JSON para o Logstash. O Logstash grava no índice `split-payment-<data>` do Elasticsearch, e o Kibana lê esse índice.

O campo `correlationId` vai como campo próprio, então dá para filtrar uma transação inteira pelo E2EID.

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| Só log no console | não dá para consultar histórico nem agregar |
| Só métrica | não responde qual transação falhou |
| Enviar log direto do serviço ao Elasticsearch | acopla a aplicação ao índice e ao formato de armazenamento |
| Usar apenas trace | exigiria coletor e backend extra, e ainda não responderia volume |

## Consequências

Positivas:

- As três perguntas têm resposta.
- O ambiente de observabilidade é opcional: sobe por arquivo de complemento, sem pesar no ambiente base.
- A correlação é o que amarra as três camadas.

Negativas:

- A stack de Elasticsearch, Logstash e Kibana consome memória, cerca de 1,5 GB.
- Sem o Logstash no ar, o appender fica tentando reconectar e registra aviso a cada 30 segundos.
- Não há tracing distribuído nem dashboard versionado no repositório.
