# Roteiro de demonstração

Cinco minutos, do zero ao fechamento. Cada passo tem o comando, o que mostrar e a frase que explica o porquê.

## Antes de começar

```bash
docker compose -f docker-compose.yml -f docker-compose.observabilidade.yml up -d
```

Espere os oito containers ficarem saudáveis:

```bash
docker ps --format "{{.Names}}\t{{.Status}}"
```

Abre estas abas, nesta ordem, para não procurar nada no meio da conversa:

| Aba | Endereço |
|---|---|
| Kafka | http://localhost:8090 |
| Kibana | http://localhost:5601/app/discover |
| Métricas do envio | http://localhost:8082/actuator/prometheus |
| Carga | http://localhost:8084/actuator/health |

---

## Passo 1: publicar um pagamento

**No Kafka UI**, tópico `pagamento.liquidado.v1`, botão de publicar:

```json
{
  "endToEndId": "E9999901020261117210000000000001",
  "valor": { "centavos": 1000000 },
  "pagador": {"ispb":"00038166","nome":"Fulano","documento":"70000000000","agencia":"3000","conta":"500000","tipoConta":"CACC"},
  "recebedor": {"ispb":"99999010","nome":"Beltrano","documento":"80000000000","agencia":"4000","conta":"600000","tipoConta":"SVGS"},
  "finalidade": "IPAY",
  "mensagem": "demonstracao",
  "liquidadoEm": "2026-11-17T21:00:00Z"
}
```

**Mostre** o log do `split-service`:

```
docker logs split-split-service --since 2m
```

> "O cálculo é do PSP, não do governo. A plataforma do Fisco não calcula nada, só recebe o que informamos."

---

## Passo 2: seguir a transação

**Abra** o dashboard: `http://localhost:5601/app/dashboards` e escolha **Split Payment**.

Ele já vem com o período fixado em 24 horas e quatro painéis, na ordem do fluxo:

| Painel | O que mostra |
|---|---|
| 1. Cálculo da segregação | linhas do `split-service` |
| 2. Envio ao SPI | linhas do `spi-envio-service`, com o `correlationId` |
| 3. Aceites do simulador | linhas do `spi-simulator` |
| 4. Erros e avisos | deve estar zerado |

**Mostre** o mesmo `correlationId` aparecendo nos painéis 2 e 3.

> "O identificador da transação é a chave da mensagem no Kafka, e viaja no contexto de log até a chamada HTTP. Por isso o mesmo valor aparece nas duas pontas."

### Se quiser ver os três serviços na mesma busca

Abra o Discover e busque o identificador como texto livre, sem nome de campo:

```
E9999901020261118014200000000005
```

A busca por campo `correlationId` traz só o envio, porque o `split-service` e o simulador ainda não têm o interceptor que preenche esse campo.

---

## Passo 3: mostrar a métrica

**Em** `http://localhost:8082/actuator/prometheus`, procure:

```
pacs008_envios_total{resultado="enviado"}
```

> "Contador de negócio, não de infraestrutura. Diz quantos pagamentos a plataforma enviou, e a latência de cada envio está no timer ao lado."

---

## Passo 4: derrubar o simulador

```bash
docker stop split-spi-simulator
```

Publique outro pagamento. Espere trinta segundos e mostre:

```bash
docker run --rm --network split-payment-adapter_default apache/kafka:3.9.0 \
  /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server kafka:19092 \
  --topic segregacao.calculada.v1-dlt --from-beginning --max-messages 1
```

Ou, mais simples, abra o tópico no Kafka UI.

> "Três tentativas e a mensagem vai para o tópico de erro, com chave e correlação. Sem isso, uma mensagem inválida trava a partição e o fluxo inteiro para."

Medido: com o simulador fora, **30 mensagens publicadas, 22 se recuperaram** quando ele voltou dentro da janela de tentativas, e **7 foram para a DLQ**. É o comportamento esperado: falha parcial, sem perder nada em silêncio.

Depois religue:

```bash
docker start split-spi-simulator
```

---

## Passo 5: volume

```bash
curl -X POST "http://localhost:8084/carga?taxa=100&segundos=10"
```

**Mostre** o Kafka UI, aba de consumer groups, e o lag.

> "Neste teste a fila ficou zerada em todos os degraus. O cálculo acompanha em tempo real, e o envio drena a fila a cerca de cento e trinta mensagens por segundo. O limite não foi a máquina: com seis mil cento e quarenta e cinco mensagens em dez segundos, o processador chegou a setenta e dois por cento e a memória nem se moveu."

Tabela medida nesta máquina:

| Oferta | Publicados | Calculados | Enviados | Lag | CPU pico |
|---|---|---|---|---|---|
| 30 por segundo | 294 | 294 | 294 | 0 | 21% |
| 100 por segundo | 913 | 913 | 913 | 0 | 29% |
| 200 por segundo | 1.613 | 1.613 | 1.613 | 0 | 54% |
| 400 por segundo | 3.480 | 3.480 | 3.480 | 0 | 65% |
| 800 por segundo | 6.145 | 6.145 | 6.145 | 0 | 72% |

Zero duplicata, zero recusa, zero mensagem morta. A máquina de doze núcleos e trinta e dois gigabytes nunca foi o gargalo.

---

## Passo 6: fechar

Mostre o README e diga:

> "São setenta e um testes em cinco camadas. A de contrato valida contra o XSD oficial do Banco Central, e foi ela que descobriu um EndToEndId fora do padrão que eu mesmo gerei."
>
> "O retorno do SPI está fora do escopo, e está documentado com o motivo: sem status de liquidação, qualquer relatório financeiro seria especulação."

---

## Se perguntarem o que falta

| Pergunta | Resposta |
|---|---|
| "E o retorno do SPI?" | fora do escopo, documentado. Sem ele não há status de liquidação |
| "Onde está o banco de dados?" | não há. O status vive em tópico compactado, e o simulador guarda em memória |
| "Isso escala?" | o cálculo sim. O envio não, e eu medi onde |
| "Por que não usou Kafka Streams?" | não havia processamento de stream a fazer. Consumidor simples é mais fácil de operar |

---

## Se algo quebrar no meio

| Sintoma | O que fazer |
|---|---|
| Kibana sem dados | o Logstash leva até 30 segundos para reconectar. Espere e publique de novo |
| 409 no envio | o simulador já processou aquele E2EID. Troque o identificador |
| mensagem não sai do tópico | confira se o `split-service` está de pé: `docker ps` |
