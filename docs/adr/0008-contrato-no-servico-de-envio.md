# ADR-0008: o contrato do pacs.008 vive no serviço que envia

- Status: aceito, substitui a decisão anterior de módulo compartilhado
- Data: 2026-11-17

## Contexto

O projeto tem dois lados que falam `pacs.008`: o `spi-envio-service`, que produz a mensagem, e o `spi-simulator`, que a recebe fazendo o papel do SPI.

A primeira versão extraiu um módulo `pacs008-contract` para os dois lados compartilharem os schemas e o codec. Na prática, isso não corresponde à realidade nem à arquitetura de referência.

Na arquitetura de referência, um adapter de envio primário carrega dentro dele os schemas, as classes geradas e o codec: `src/main/resources/xsd` e o pacote gerado `br.gov.bcb.pi.pacs008` convivem no mesmo projeto que monta e envia. Não existe módulo de contrato separado.

E o SPI é um sistema externo. Ele tem a própria implementação do schema, publicada pelo Banco Central. Não faz sentido que o simulador dependa do código de quem envia.

## Decisão

O contrato do pacs.008 fica dentro do `spi-envio-service`.

| Peça | Onde fica |
|---|---|
| Schemas oficiais | `spi-envio-service/src/main/resources/xsd` |
| Classes JAXB | geradas no build, pacote `br.gov.bcb.pi.pacs008` |
| Codec, assinador | `br.com.paymentslab.spienvio.pacs008` |

O `spi-simulator` tem a própria cópia dos dois XSDs e a própria validação, sem dependência de código do emissor.

## Alternativas consideradas

| Alternativa | Por que não |
|---|---|
| Módulo compartilhado de contrato | não corresponde ao desenho de referência e cria acoplamento entre lados que deveriam ser independentes |
| Simulador depender do serviço de envio | inverte a direção: o sistema externo passaria a depender de quem o chama |
| Duplicar também o codec e o assinador | desnecessário. O simulador só valida e lê o identificador, não assina nem gera |

## Consequências

Positivas:

- O serviço de envio é autossuficiente: um clone do módulo basta para compilá-lo e testá-lo.
- O simulador é honestamente externo. Se o emissor gerar um XML torto, a recusa acontece do outro lado, sem código compartilhado que mascare o erro.
- O reactor tem três módulos, todos aplicações, sem biblioteca intermediária.

Negativas:

- Os dois XSDs existem em duas cópias no repositório. É duplicação de dado oficial, não de código, e cada lado pode evoluir a versão do schema no seu ritmo.
- Trocar a versão do schema exige atualizar os dois módulos.
