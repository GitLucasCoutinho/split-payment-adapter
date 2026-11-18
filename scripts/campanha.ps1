<#
.SYNOPSIS
    Campanha de demonstracao com carga, falha induzida, mensagem morta e reprocessamento.

.DESCRIPTION
    Roda em quatro fases, com cerca de quinze minutos:

      1. carga em pulsos, para o grafico ganhar forma
      2. falha induzida: o SPI sai do ar e as mensagens vao para a DLQ
      3. carga de novo, agora com o SPI no ar
      4. reprocessamento: as mensagens da DLQ voltam ao topico original

    A fase 4 e a que fecha a historia: nada foi perdido, apenas adiado.

.EXAMPLE
    .\campanha.ps1
    .\campanha.ps1 -Repo "C:\caminho\do\repositorio"
#>
param(
    [string]$Repo = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path,
    [int]$PausaCurta = 60,
    [int]$PausaLonga = 90
)

$ErrorActionPreference = "Continue"
$arquivosCompose = @(
    "-f", (Join-Path $Repo "docker-compose.yml"),
    "-f", (Join-Path $Repo "docker-compose.observabilidade.yml")
)
$topicoPrincipal = "segregacao.calculada.v1"
$topicoErro = "segregacao.calculada.v1-dlt"
$inicio = Get-Date

function Escreve($mensagem) {
    $decorrido = [int]((Get-Date) - $inicio).TotalMinutes
    Write-Host ("[{0,2}min] {1}" -f $decorrido, $mensagem)
}

function Carga([int]$taxa, [int]$segundos) {
    $r = Invoke-RestMethod -Method Post -Uri "http://localhost:8084/carga?taxa=$taxa&segundos=$segundos" -TimeoutSec ($segundos + 120)
    return [int]$r.publicados
}

function Aguarda([int]$segundos) {
    Start-Sleep -Seconds $segundos
}

function ContaTopico([string]$topico) {
    $saida = docker exec split-kafka /opt/kafka/bin/kafka-get-offsets.sh --bootstrap-server localhost:19092 --topic $topico 2>$null
    $soma = 0
    foreach ($linha in $saida) {
        if ("$linha" -match ':(\d+)\s*$') { $soma += [int]$Matches[1] }
    }
    return $soma
}

function Reprocessa([int]$quantidade) {
    if ($quantidade -le 0) { return 0 }
    $comando = "/opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:19092 " +
               "--topic $topicoErro --from-beginning --max-messages $quantidade --timeout-ms 30000 " +
               "--property print.key=true --property key.separator='|' 2>/dev/null | " +
               "/opt/kafka/bin/kafka-console-producer.sh --bootstrap-server localhost:19092 " +
               "--topic $topicoPrincipal --property parse.key=true --property key.separator='|' 2>/dev/null"
    docker exec split-kafka sh -c $comando
    return $quantidade
}

Write-Host ""
Write-Host "=== campanha de demonstracao ===" -ForegroundColor Cyan
Write-Host "repo: $Repo"
Write-Host ""

# ---------------------------------------------------------------- fase 1
Escreve "fase 1 de 4: carga em pulsos"
$totalPublicado = 0
foreach ($pulso in @(
    @{ taxa = 20; segundos = 20 },
    @{ taxa = 60; segundos = 25 },
    @{ taxa = 15; segundos = 20 },
    @{ taxa = 40; segundos = 20 }
)) {
    $publicados = Carga $pulso.taxa $pulso.segundos
    $totalPublicado += $publicados
    Escreve ("  {0}/s por {1}s -> {2} publicados" -f $pulso.taxa, $pulso.segundos, $publicados)
    Aguarda $PausaCurta
}

# ---------------------------------------------------------------- fase 2
Escreve "fase 2 de 4: derrubando o SPI para gerar mensagem morta"
docker compose @arquivosCompose stop spi-simulator 2>$null | Out-Null
Aguarda 5

$publicadosComFalha = Carga 2 10
$totalPublicado += $publicadosComFalha
Escreve "  $publicadosComFalha publicados com o SPI fora"

Escreve "  aguardando as tentativas terminarem"
Aguarda 180
$naDlq = ContaTopico $topicoErro
Escreve "  mensagens na DLQ: $naDlq"

Escreve "fase 3 de 4: religando o SPI"
docker compose @arquivosCompose start spi-simulator 2>$null | Out-Null
Aguarda 40

# ---------------------------------------------------------------- fase 3
foreach ($pulso in @(
    @{ taxa = 30; segundos = 20 },
    @{ taxa = 10; segundos = 15 },
    @{ taxa = 50; segundos = 20 }
)) {
    $publicados = Carga $pulso.taxa $pulso.segundos
    $totalPublicado += $publicados
    Escreve ("  {0}/s por {1}s -> {2} publicados" -f $pulso.taxa, $pulso.segundos, $publicados)
    Aguarda $PausaLonga
}

# ---------------------------------------------------------------- fase 4
Escreve "fase 4 de 4: reprocessando a DLQ"
$antes = ContaTopico $topicoErro
$reprocessadas = Reprocessa $antes
Escreve "  $reprocessadas mensagens devolvidas ao topico principal"
Aguarda 45

$enviados = ((Invoke-WebRequest "http://localhost:8082/actuator/prometheus" -UseBasicParsing -TimeoutSec 15).Content -split "`n" |
    Where-Object { $_ -like "pacs008_envios_total{*enviado*" } | Select-Object -First 1 | ForEach-Object { ($_ -split '\s+')[-1] })
$falhas = ((Invoke-WebRequest "http://localhost:8082/actuator/prometheus" -UseBasicParsing -TimeoutSec 15).Content -split "`n" |
    Where-Object { $_ -like "pacs008_envios_total{*falha*" } | Select-Object -First 1 | ForEach-Object { ($_ -split '\s+')[-1] })

Write-Host ""
Write-Host "=== resumo ===" -ForegroundColor Cyan
Write-Host "publicados na campanha : $totalPublicado"
Write-Host "foram para a DLQ       : $naDlq"
Write-Host "reprocessadas da DLQ   : $reprocessadas"
Write-Host "enviados ao SPI        : $enviados"
Write-Host "tentativas com falha   : $falhas"
Write-Host ""
Escreve "fim. Abra o dashboard no Kibana e o topico $topicoErro no Kafka UI."
