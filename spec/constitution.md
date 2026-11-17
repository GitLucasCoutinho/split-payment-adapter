# Constitution

Princípios não negociáveis da plataforma. Toda decisão de código, teste ou infraestrutura deriva daqui. Quando houver conflito entre velocidade e um artigo, o artigo vence.

## Artigo 1. Dinheiro é inteiro

Todo valor monetário é representado em centavos, com tipo próprio. Ponto flutuante é proibido em qualquer camada, inclusive em DTO, em evento e em log.

## Artigo 2. Arredondamento sempre para baixo

O CBS/IBS Segregado é truncado, nunca arredondado. Arredondar para cima retém mais tributo do que o devido, e isso é erro fiscal.

## Artigo 3. Idempotência por E2EID

O E2EID (End-to-End ID, identificador único ponta a ponta da transação Pix) é a chave em toda fronteira. A mesma transação, reenviada N vezes, produz um único efeito em cada serviço.

## Artigo 4. Ordem é regra, não convenção

Informe fora da sequência prevista é rejeitado pela máquina de estados. Nenhum adaptador envia passo 2 sem passo 1.

## Artigo 5. O domínio não conhece framework

Os pacotes de domínio e aplicação não importam Spring, Kafka, Jackson ou qualquer biblioteca de infraestrutura. A regra é verificada por teste, não por disciplina.

## Artigo 6. Toda saída é vigiada

Toda chamada externa tem timeout explícito, retry com limite e destino para falha definitiva. Nenhuma chamada espera para sempre.

## Artigo 7. Segredo não mora no repositório

Nenhuma credencial em código, em YAML versionado ou em imagem publicada. O ambiente injeta.

## Artigo 8. Toda regra tem teste que a nomeia

Regra de negócio sem teste que a cite pelo nome não existe. O nome do teste descreve a regra, não o método.

## Artigo 9. Métrica tem nome estável e unidade no nome

Toda métrica segue o padrão `<servico>_<assunto>_<unidade>`. Renomear métrica é quebra de contrato e exige ADR.

## Artigo 10. A spec é a fonte de verdade

Código que diverge da spec é bug, mesmo que funcione. Mudou o comportamento, muda a spec no mesmo commit.

## Artigo 11. Cada serviço tem um dono de dado

Um dado tem um único serviço responsável por produzi-lo. Os demais consomem por evento ou por contrato, nunca por acesso direto ao banco alheio.

## Artigo 12. Contrato entre serviços é versionado

Todo evento e toda mensagem entre serviços tem nome versionado, schema publicado e teste de compatibilidade. Mudança incompatível exige versão nova, não alteração silenciosa.

## Artigo 13. O contrato externo não é nosso

O pacs.008 é definido pelo Banco Central. Nós não alteramos o schema, adaptamos o nosso modelo a ele. Divergência se resolve no mapeador, nunca no XSD.

---

## Como cada artigo é verificado

| Artigo | Verificação |
|---|---|
| 1 | teste de arquitetura proibindo ponto flutuante em valor monetário |
| 2 | teste unitário com casos de truncamento |
| 3 | teste de integração com chamada duplicada em cada serviço |
| 4 | teste de transição inválida |
| 5 | teste de arquitetura por leitura de imports |
| 6 | configuração obrigatória e teste de falha |
| 7 | revisão de configuração e teste de profile |
| 8 | cobertura e revisão de nomes |
| 9 | teste que valida o catálogo de métricas |
| 10 | matriz de rastreabilidade em `traceability.md` |
| 11 | revisão de dependência entre módulos |
| 12 | teste de contrato do evento |
| 13 | validação contra o XSD em todo envio |

A tabela acima é o contrato do projeto com ele mesmo. Se um artigo não tem verificação, ele é intenção, não princípio.
