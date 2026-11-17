# ADR-0007: Decomposição em três serviços

- Status: aceito
- Data: 2026-11-17

## Contexto

O projeto poderia ser um serviço único que consome o pagamento, calcula o tributo, monta o XML e envia. Funciona, e é mais simples de operar.

Por outro lado, as partes mudam por motivos diferentes: a regra tributária muda por lei, o layout da mensagem muda por versão de schema, e o simulador existe apenas para teste.

## Decisão

Separar em três aplicações independentes, com um módulo de contrato compartilhado.

| Serviço | Responsabilidade | Dono do dado |
|---|---|---|
| `split-service` | cálculo da segregação | resultado do cálculo |
| `spi-envio-service` | montagem, assinatura e envio | XML e protocolo |
| `spi-simulator` | recepção simulada | protocolos emitidos |

**O critério não é SOLID.** SOLID é sobre classes. O critério é: ciclos de mudança diferentes, isolamento de falha e escala diferente.

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| Serviço único | mudar a regra tributária obrigaria a redeployar a mensageria |
| Dois serviços, com o simulador embutido | o simulador deixaria de ser independente, e o teste perderia valor |
| Três repositórios | custo de governança sem ganho neste tamanho |

## Consequências

Positivas:

- Mudança de alíquota não toca no código de mensageria.
- Simulador fora do ar não impede o cálculo.
- Cada serviço escala e é observado separadamente.
- A conversa de arquitetura fica defensável com argumento técnico, não com sigla.

Negativas:

- Consistência eventual entre cálculo e envio.
- Mais três aplicações para subir e monitorar.
- Necessidade de contrato de evento versionado.
