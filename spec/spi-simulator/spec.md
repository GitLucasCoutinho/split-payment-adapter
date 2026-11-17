# Spec: spi-simulator

| Campo | Valor |
|---|---|
| Serviço | `spi-simulator` |
| Porta | 8083 |
| Entrada | `POST /spi/mensagens` com XML assinado |
| Papel | fazer o papel do SPI no projeto |

## 1. Escopo

Receber o pacs.008, validar contra o XSD, recusar duplicidade e devolver um protocolo. Não processa pagamento e não calcula tributo.

O simulador não é um eco: ele valida de verdade. Se o mapper gerar um XML torto, o erro aparece aqui.

## 2. Requisitos funcionais

**RM-1, validação**
QUANDO uma mensagem chega, O SISTEMA DEVE validá-la contra o `pacs.008.spi.1.15.xsd`.

**RM-2, recusa por contrato**
SE a mensagem não validar, ENTÃO O SISTEMA DEVE responder `400` com o motivo da validação.

**RM-3, recusa por duplicidade**
SE o E2EID (End-to-End ID) já foi processado, ENTÃO O SISTEMA DEVE responder `409` com o identificador repetido.

**RM-4, protocolo**
QUANDO a mensagem é aceita, O SISTEMA DEVE devolver `200` com número de protocolo, data de recebimento e E2EID.

**RM-5, rastreabilidade**
O SISTEMA DEVE registrar em log o E2EID, o protocolo e o tamanho da mensagem recebida.

## 3. Requisitos não funcionais

| ID | Requisito | SLI | Alvo |
|---|---|---|---|
| NFM-1 | Latência da validação | p99 de `simulador_validacao_segundos` | menor que 100 ms |
| NFM-2 | Disponibilidade | 1 menos 5xx | 99,9% |
| NFM-3 | Mensagem inválida aceita | contador | zero |

## 4. Critérios de aceite

| # | Dado | Quando | Então |
|---|---|---|---|
| CA-1 | mensagem assinada e conforme | é enviada | `200` com protocolo |
| CA-2 | mensagem sem assinatura | é enviada | `400` com motivo |
| CA-3 | a mesma mensagem duas vezes | é enviada | a segunda recebe `409` |
| CA-4 | mensagem aceita | é inspecionada | o log tem E2EID e protocolo |

## 5. Testes

| Requisito | Teste | Nível |
|---|---|---|
| RM-1, RM-4 | `deve_aceitar_mensagem_assinada_e_conforme` | integração com MockMvc |
| RM-2 | `deve_recusar_mensagem_sem_assinatura` | integração com MockMvc |
| RM-3 | `deve_recusar_mensagem_duplicada` | integração com MockMvc |
