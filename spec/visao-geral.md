# Visão geral da plataforma

Documento que atravessa serviços. Define os contextos, o fluxo ponta a ponta e o catálogo de eventos. Requisito de comportamento fica na spec de cada serviço.

## Problema

Com o Split Payment, o estabelecimento deixa de receber o valor cheio. Na liquidação, a parcela de IBS (Imposto sobre Bens e Serviços) e CBS (Contribuição sobre Bens e Serviços) é separada e recolhida ao Fisco. Quem processa o pagamento precisa calcular o tributo, informar a Plataforma Pública e provar que o valor retido está correto.

## Contextos

| Contexto | Serviço | Responsabilidade | Dado que possui |
|---|---|---|---|
| Segregação tributária | `split-service` | calcular IBS e CBS de um pagamento liquidado | o resultado do cálculo |
| Mensageria do SPI | `spi-envio-service` | montar, assinar e enviar mensagens ao SPI | o XML e o protocolo de envio |
| Recepção simulada | `spi-simulator` | validar e protocolizar a mensagem recebida | os protocolos emitidos |

Cada serviço tem o seu próprio grupo de consumer. Nenhum serviço lê o banco do outro.

## Fluxo ponta a ponta

```
Kafka UI (8090)
   │ publica o pagamento liquidado
   ▼
pagamento.liquidado.v1
   │
   ▼
[split-service 8081]
   │ calcula IBS e CBS, trunca para baixo
   ▼
segregacao.calculada.v1
   │
   ▼
[spi-envio-service 8082]
   │ mapeia para pacs.008, assina, valida contra o XSD
   ▼
HTTP POST /spi/mensagens
   │
   ▼
[spi-simulator 8083]
   │ valida contra o XSD, recusa duplicata, emite protocolo
   ▼
200 { protocolo, recebidoEm }
```

## Catálogo de eventos

| Tópico | Versão | Produtor | Consumidor | Chave | Conteúdo |
|---|---|---|---|---|---|
| `pagamento.liquidado.v1` | 1 | sistema interno, manual na interface | `split-service` | E2EID | pagamento e participantes |
| `segregacao.calculada.v1` | 1 | `split-service` | `spi-envio-service` | E2EID | valores da segregação e dados do pagamento |

A chave é o E2EID nos dois tópicos. Isso garante ordem por transação e permite idempotência no consumidor.

## Contrato compartilhado

O contrato do `pacs.008` vive dentro do `spi-envio-service`, junto com os XSDs e o codec. O `spi-simulator` tem a própria cópia dos schemas, porque representa um sistema externo. A decisão está no [ADR-0008](../docs/adr/0008-contrato-no-servico-de-envio.md).

É compartilhado por um motivo legítimo: `spi-envio-service` produz a mensagem e `spi-simulator` a consome. Os dois lados precisam falar o mesmo contrato. Contrato compartilhado não é acoplamento de negócio.

O que **não** é compartilhado: modelo de domínio, evento de segregação e DTO de pagamento. Cada serviço define o seu.

## O que cada serviço não faz

| Serviço | Não faz |
|---|---|
| `split-service` | não gera XML, não conhece pacs.008, não chama HTTP |
| `spi-envio-service` | não calcula tributo, não decide alíquota |
| `spi-simulator` | não processa pagamento, não calcula nada |

## Ambientes

| Ambiente | Como sobe | Kafka | Simulador |
|---|---|---|---|
| local | `mvn spring-boot:run` | `localhost:9092` | `localhost:8083` |
| docker | `docker compose up` | `kafka:19092` | `spi-simulator:8083` |
| teste | `mvn test` | não sobe broker | mock ou MockMvc |

## Fora de escopo

Boleto, Pix Automático, Pix Estático, TED, TEF, MOC (Mecanismo de Ocorrências), repasse financeiro e cartão. O motivo está em `docs/split-payment/contexto-regulatorio.md`.
