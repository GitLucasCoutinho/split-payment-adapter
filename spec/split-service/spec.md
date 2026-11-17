# Spec: split-service

| Campo | Valor |
|---|---|
| Serviço | `split-service` |
| Porta | 8081 |
| Entrada | `pagamento.liquidado.v1` |
| Saída | `segregacao.calculada.v1` |
| Fonte normativa | Manual de Operações, capítulo de cálculo |

## 1. Escopo

Consumir o pagamento liquidado, calcular a segregação de IBS e CBS e publicar o resultado. Não gera XML, não chama HTTP, não conhece o SPI.

## 2. Requisitos funcionais

**RS-1, cálculo**
QUANDO um pagamento liquidado chega, O SISTEMA DEVE calcular o valor de IBS e de CBS sobre o valor bruto, truncando o resultado para baixo.

**RS-2, fechamento**
O SISTEMA DEVE garantir que valor bruto é igual à soma de líquido, IBS e CBS. Se a soma não fechar, o cálculo é recusado.

**RS-3, evento**
QUANDO o cálculo termina, O SISTEMA DEVE publicar `segregacao.calculada.v1` com chave igual ao E2EID (End-to-End ID, identificador único ponta a ponta), contendo os valores e os dados do pagamento.

**RS-4, idempotência**
QUANDO o mesmo E2EID chega novamente, O SISTEMA DEVE produzir o mesmo resultado, sem efeito duplicado.

**RS-5, valor inválido**
SE o valor for menor ou igual a zero, ENTÃO O SISTEMA DEVE recusar sem publicar evento.

## 3. Requisitos não funcionais

| ID | Requisito | SLI | Alvo |
|---|---|---|---|
| NFS-1 | Latência do cálculo | p99 de `split_calculo_segundos` | menor que 50 ms |
| NFS-2 | Disponibilidade | 1 menos 5xx | 99,9% em 30 dias |
| NFS-3 | Rastreabilidade | eventos com trace completo | 100% |
| NFS-4 | Cobertura | linha e branch | 80% e 70%, domínio 90% e 85% |

## 4. Critérios de aceite

| # | Dado | Quando | Então |
|---|---|---|---|
| CA-1 | pagamento de R$ 1.000,00 | é processado | IBS de R$ 1,00 e CBS de R$ 9,00 |
| CA-2 | pagamento de 111 centavos | é processado | IBS e CBS zero, líquido de 111 |
| CA-3 | pagamento de 112 centavos | é processado | CBS de 1 centavo |
| CA-4 | qualquer valor entre 1 e 2.000 centavos | é processado | a soma das partes iguala o bruto |
| CA-5 | o mesmo E2EID duas vezes | é processado | o resultado é o mesmo nas duas |

## 5. Testes

| Requisito | Teste | Nível |
|---|---|---|
| RS-1 | `deve_calcular_cbs_e_ibs_sobre_mil_reais` | unitário |
| RS-1 | `deve_truncar_para_baixo_e_nunca_arredondar_para_cima` | unitário |
| RS-2 | `a_segregacao_deve_fechar_em_qualquer_valor` | unitário |
| RS-2 | `os_tributos_nunca_devem_passar_do_valor_bruto` | unitário |
