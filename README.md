# Split Payment Platform

![CI](https://github.com/GitLucasCoutinho/split-payment-adapter/actions/workflows/ci.yml/badge.svg)

Plataforma de referência para Split Payment no Pix Dinâmico: calcula a segregação de IBS e CBS de um pagamento liquidado, monta a mensagem pacs.008 do SPI (Sistema de Pagamentos Instantâneos), assina, valida contra o XSD oficial e envia a um simulador que faz o papel do Banco Central.

Status: três serviços funcionando, 41 testes verdes, cobertura acima de 85% e especificação por serviço escrita.

## O problema

Com o Split Payment, o estabelecimento deixa de receber o valor cheio. Na liquidação, a parcela de IBS (Imposto sobre Bens e Serviços) e CBS (Contribuição sobre Bens e Serviços) é separada e recolhida ao Fisco.

Quem processa o pagamento precisa calcular o tributo, informar a plataforma do governo e provar que o valor retido está correto. Errar isso não é bug de software: é erro fiscal, com multa, e o lojista perde caixa sem entender por quê.

## Arquitetura

Três aplicações independentes e um módulo de contrato. Cada serviço tem seu próprio grupo de consumer e seu próprio deploy.

| Módulo | Tipo | Responsabilidade | Porta |
|---|---|---|---|
| [`split-service`](split-service/README.md) | aplicação | calcula a segregação tributária | 8081 |
| [`spi-envio-service`](spi-envio-service/README.md) | aplicação | fluxo de envio ao SPI: monta, assina e envia | 8082 |
| [`spi-simulator`](spi-simulator/README.md) | aplicação | valida e protocoliza a mensagem | 8083 |

Cada módulo tem o seu README com contexto, tecnologias, diagrama de integração e limites conhecidos.

O `spi-envio-service` carrega o contrato do pacs.008 dentro dele: os XSDs em `src/main/resources/xsd`, as classes JAXB geradas no build e o codec com a assinatura. É o mesmo desenho de um adapter de envio primário: quem envia é dono do contrato que usa.

A unidade de deploy é o **fluxo**, não o tipo de mensagem. O `spi-envio-service` trata o envio primário e hoje tem o `pacs.008` como primeiro caso de uso. Um tipo novo entra como mais uma classe, não como mais um serviço.

## Como funciona

![Fluxo completo: do simulador de transações ao aceite do SPI](docs/imagens/fluxo-completo.png)

Duas setas são tópicos do Kafka e uma é chamada HTTP. Se o envio falhar três vezes, a mensagem sai do fluxo e vai para a fila de erro, com payload e correlação.

## O que está implementado e o que não está

**Implementado:** cálculo da segregação, geração do pacs.008 a partir do modelo de domínio, assinatura com o perfil de assinatura do SPI, validação contra o XSD oficial, envio HTTP, simulador que valida de verdade e correlação de ponta a ponta pelo E2EID.

**Não implementado:** o envio dos informes à Plataforma Pública do Split Payment (transação iniciada, preliminar de pagamento, segregação em três passos) e o consumo do Retorno Super Inteligente. Está especificado na documentação regulatória e é o próximo passo.

Essa distinção importa: o que roda hoje é a perna de pagamento mais o cálculo. O informe ao Fisco é o que falta.

## Stack

| Camada | Escolha |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.5 |
| Mensageria | Kafka com spring-kafka |
| Contrato | JAXB gerado do XSD oficial do SPI, pacote 5.12.1 do Banco Central |
| Observabilidade | Micrometer e actuator |
| Cobertura | JaCoCo com gate no build |
| CI/CD | GitHub Actions |

## Como rodar

```bash
mvn -DskipTests package
docker compose up -d --build
```

Depois:

| Endereço | O que é |
|---|---|
| `http://localhost:8090` | interface do Kafka, onde você publica a mensagem |
| `http://localhost:8081/actuator/health` | split-service |
| `http://localhost:8082/actuator/health` | spi-envio-service |
| `http://localhost:8083/actuator/health` | spi-simulator |

Com observabilidade, sobe também Elasticsearch, Logstash e Kibana:

```bash
docker compose -f docker-compose.yml -f docker-compose.observabilidade.yml up -d
```

| Endereço | O que é |
|---|---|
| `http://localhost:5601` | Kibana, com a visão de dados `split-payment-*` já criada |
| `http://localhost:9200` | Elasticsearch |
| `http://localhost:8081/actuator/prometheus` | métricas do split-service |
| `http://localhost:8082/actuator/prometheus` | métricas do spi-envio-service |
| `http://localhost:8083/actuator/prometheus` | métricas do spi-simulator |

Para rodar só os testes, sem broker e sem Docker:

```bash
mvn test
```

## Observabilidade

Três camadas, cada uma respondendo uma pergunta diferente.

| Camada | Ferramenta | Responde |
|---|---|---|
| Métrica | Micrometer com registro Prometheus | quantas transações e com que latência |
| Log estruturado | JSON no Elasticsearch, consultado no Kibana | o que aconteceu, linha por linha |
| Correlação | E2EID no MDC, do Kafka até o HTTP | por que aquela transação |

As métricas de negócio são `split_pagamentos_total`, `split_calculo_seconds`, `pacs008_envios_total`, `pacs008_envio_seconds`, `simulador_mensagens_total` e `simulador_validacao_seconds`.

No Kibana, filtre por `correlationId` e você vê a transação inteira: o cálculo, o envio e o aceite, com o mesmo identificador.

## Tratamento de erro

Cada consumidor reprocessa a mensagem algumas vezes. Se continuar falhando, ela vai para `<tópico>-dlt`, preservando chave, correlação e a causa da falha.

O padrão é conhecido como **dead letter queue**, fila de mensagem morta. No Kafka ele ganha outro nome, **dead letter topic**, tópico de mensagem morta, porque o Kafka não tem fila, tem tópico. É por isso que o sufixo é `-dlt` e não `-dlq`.

Isso evita o pior cenário de Kafka: uma mensagem inválida travando a partição para sempre. A decisão e os limites estão no [ADR-0011](docs/adr/0011-mensagem-morta-em-topico-de-erro.md).

## Testes

**72 testes em cinco camadas.** A forma é proposital: o que é barato roda em todo build, e o que é caro roda só onde o risco justifica.

![Pirâmide de testes: 72 testes em cinco camadas](docs/imagens/piramide-testes.png)

| Camada | Testes | O que prova | Onde |
|---|---|---|---|
| Unidade | 54 | a regra de negócio pura, sem framework e sem rede | modelos, cálculo, mapeadores, gerador de carga |
| Contrato | 11 | a mensagem contra o **XSD oficial do Banco Central** | `Pacs008Codec`, `Pacs008Mapper`, `ValidadorPacs008` |
| Integração | 5 | o wiring: contexto, `MockMvc` e `MockRestServiceServer` | `ApplicationContextTest`, `SimuladorControllerTest`, `EnviadorHttpTest` |
| Arquitetura | 2 | a dependência aponta para dentro e o núcleo não importa framework | `ArquiteturaTest` com ArchUnit |
| Ponta a ponta | 0 | o ciclo completo, hoje à mão no Docker | `docs/demonstracao.md` |

A camada de contrato é a que pega o erro caro. Um campo fora do formato ou uma assinatura fora do perfil do SPI só aparece quando o Banco Central recusa. Aqui aparece no build. Foi ela que descobriu um EndToEndId de 30 caracteres onde o schema exige 32.

A camada de arquitetura era revisão manual. Virou teste: importar Spring dentro de `dominio` quebra o build.

```bash
mvn test      # 72 testes, sem Docker e sem broker
mvn verify    # o mesmo, mais o gate de cobertura
```

O detalhamento de cada camada e o que ainda falta estão em [spec/testes.md](spec/testes.md).

## Documentação

| Documento | Conteúdo |
|---|---|
| [Constitution](spec/constitution.md) | 13 princípios e como cada um é verificado |
| [Visão geral](spec/visao-geral.md) | contextos, fluxo e catálogo de eventos |
| [Spec do split-service](spec/split-service/spec.md) | requisitos do cálculo |
| [Spec do spi-envio-service](spec/spi-envio-service/spec.md) | requisitos de montagem, assinatura e envio |
| [Spec do spi-simulator](spec/spi-simulator/spec.md) | requisitos de validação e protocolo |
| [Rastreabilidade](spec/traceability.md) | requisito, serviço, teste e métrica |
| [Pirâmide de testes](spec/testes.md) | as cinco camadas, os números e o que falta |
| [Roteiro de demonstração](docs/demonstracao.md) | cinco minutos, passo a passo, com a frase de cada tela |
| [Decisões de arquitetura](docs/adr/) | 10 ADRs com contexto e alternativa descartada |
| [Contexto regulatório](docs/split-payment/contexto-regulatorio.md) | base legal, cronograma, arranjos e o limite do cartão |
| [Notas técnicas](docs/split-payment/notas-tecnicas.md) | identificadores, streaming com token, prazos e armadilhas |
| [Glossário](docs/split-payment/glossario.md) | todos os termos e siglas do domínio |

Toda afirmação regulatória tem fonte com data de consulta. A documentação é parte do projeto, não anexo.

## Status

| Etapa | Situação |
|---|---|
| Pesquisa e leitura dos manuais oficiais | concluída |
| Contexto regulatório documentado | concluído |
| Spec por serviço e ADRs | concluídos |
| Cálculo da segregação com testes | concluído |
| Geração, assinatura e validação do pacs.008 | concluído |
| Simulador com validação e recusa de duplicata | concluído |
| Kafka entre os serviços | concluído, ciclo completo validado no Docker |
| Informes à Plataforma Pública | não iniciado |
| JaCoCo com gate no build | concluído |
| GitHub Actions de CI e CD | concluído |
| Métricas de negócio no Prometheus | concluído |
| Log estruturado no Kibana | concluído |
| Tratamento de erro com tópico de mensagem morta | concluído |

## Licença

MIT. Todos os dados são sintéticos. Os manuais oficiais do CGIBS e do Banco Central não são redistribuídos neste repositório.
