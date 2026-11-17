# Notas técnicas sobre o contrato

Observações retiradas do Manual de Integração v1.1.1, do Manual de Operações v1.0.1 e do Manual de Tempos v1.0.1. São as armadilhas que o código precisa tratar e que a spec precisa registrar.

## O retorno não é webhook

A Plataforma Pública do Split Payment devolve os eventos ao PSP (Prestador de Serviços de Pagamento) por **pull-based event streaming**, que é streaming de eventos por leitura: o PSP abre um stream e passa a fazer long polling carregando um token de posição na URL.

| Passo | O que acontece |
|---|---|
| 1 | PSP chama `/stream/start` e recebe um `streamId` e um token |
| 2 | PSP chama `/stream/{token}` e recebe mensagens e um novo token |
| 3 | Repete com o token novo, indefinidamente |
| 4 | `204` significa sem novidade, continuar |
| 5 | `410` significa stream encerrada, abrir outra |
| 6 | `DELETE` encerra a stream |

O token e o `streamId` têm validade de **2 minutos**. Stream inativa é sanitizada pela própria Plataforma.

Isso resolve três problemas de uma vez: não exige IP público, dá controle de posição e permite recuperação. Cria outros três, e é onde mora a complexidade:

- continuidade após falha, sempre retomando do último token
- token expirado no meio do consumo
- duplicidade na retomada, porque a mesma mensagem pode vir duas vezes

## Segregação em três passos

A remessa de segregação não é uma chamada, é um envelope.

| Passo | Endpoint | Papel |
|---|---|---|
| 1 | `POST /api/v1/segregacao` | abre a remessa e devolve `idInfSegr` |
| 2 | `POST /api/v1/pix-dinamico/segregacao/{idInfSegr}/lotes` | envia os lotes de transações |
| 3 | `POST /api/v1/segregacao/finalizacao` | fecha a remessa |

Se não houver transação para enviar, manda apenas o passo 1 e o passo 3.

Consequência para o código: existe estado intermediário. Uma remessa aberta e não finalizada é lixo que precisa de rotina de expiração, senão acumula.

## Cálculo

- Existe **CBS/IBS Informado** (o que o PSP informa) e **CBS/IBS Segregado** (o que efetivamente é retido).
- O cálculo de proporcionalidade só se aplica a arranjo iniciado pelo recebedor. No Pix Estático, TED e TEF, o Segregado é igual ao Informado.
- O resultado é **sempre arredondado para baixo**, com precisão definida no manual.
- Há hierarquia de valores de referência: quando o valor informado muda, o cálculo usa o mais atualizado disponível.

## Identificadores

Três nomes que parecem a mesma coisa e não são. Confundir muda a chave de idempotência do sistema.

| Identificador | O que é | Onde aparece |
|---|---|---|
| E2EID | End-to-End ID. Identificador único ponta a ponta da transação Pix | dicionário de campos do arranjo Pix |
| txid | Identificador da cobrança Pix, o QR Code, definido pelo PSP recebedor | fluxo do Pix, não no Split Payment |
| nsuId | Número sequencial da **mensagem** na Plataforma. Serve para paginação e retomada | retorno, consulta retroativa e MOC |

O Manual de Operações é explícito: o único identificador considerado no contexto do Split Payment é o E2EID. O `nsuId` não identifica a transação, identifica a mensagem.

Consequência para o código: a chave de idempotência é o E2EID. O `nsuId` é posição de leitura.

## Prazos e janelas

Números do Manual de Tempos. Servem como SLO do serviço, porque são regulatórios e não inventados.

| Item | Valor |
|---|---|
| Limite máximo de ANS em pelo menos um indicador | 30 minutos |
| Meta funcional no mesmo indicador | 5 minutos |
| Fechamento | D+1 |
| Janela de processamento após cada corte | 3 horas |
| Repasse financeiro | um por dia útil, por arranjo e por PSP, segregado por tributo |
| Prazo para solicitação de estorno | até 90 dias |

Antes de usar em código, confirmar a qual indicador cada número pertence na Seção 4.1 do Manual de Tempos.

## Armadilhas para o código

- **Ordem obrigatória.** Informe fora de sequência é erro, e o código precisa impedir por máquina de estados, não por fé.
- **Idempotência.** Retry de PSP é rotina. O mesmo E2EID (End-to-End ID, identificador único ponta a ponta da transação Pix) não pode gerar duas segregações.
- **Dinheiro em centavos.** Sem `double`, em nenhum ponto.
- **Arredondamento para baixo.** Truncar, não arredondar.
- **Rastreabilidade.** O manual tem capítulo próprio sobre requisitos de rastreabilidade. Trace e correlação não são opcionais.

## Curiosidades

- "Sprint payment" é uma confusão fonética comum com "split payment", e aparece até em material didático explicando a diferença.
- A Plataforma não calcula tributo. Ela é cano, não calculadora.
- No Pix Estático metade do motor de cálculo não roda, porque não há proporcionalidade.
- O retorno ser pull, e não webhook, é a decisão de arquitetura mais interessante da especificação.
