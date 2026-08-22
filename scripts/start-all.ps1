# ============================================================
# Sobe toda a malha de microsservicos do Prontuario Eletronico.
# Compativel com Windows PowerShell 5.1 e PowerShell 7+.
# Abre uma janela de terminal por processo (18 back-end + 1 frontend),
# na ordem certa: Eureka/Config -> microsservicos -> Gateway -> Monolito -> Frontend.
#
# Uso:
#   powershell -ExecutionPolicy Bypass -File scripts\start-all.ps1
#   powershell -ExecutionPolicy Bypass -File scripts\start-all.ps1 -SemMonolito -SemFrontend
#   powershell -ExecutionPolicy Bypass -File scripts\start-all.ps1 -MavenBin "C:\Users\taryj\apache-maven-3.9.6\bin"
#
# Parametros:
#   -SemMonolito      nao inicia o monolito (porta 8080)
#   -SemFrontend      nao inicia o frontend (npm run dev)
#   -MavenBin <path>  pasta bin do Maven, se "mvn" nao estiver no PATH desta maquina
# ============================================================
param(
  [switch]$SemMonolito,
  [switch]$SemFrontend,
  [string]$MavenBin = ""
)

$Root = Split-Path -Parent $PSScriptRoot

$Infra = @("eureka-server", "config-server")
$Microservicos = @(
  "iam-service", "paciente-service", "catalogo-service", "agendamento-service",
  "faturamento-service", "dashboard-service", "auditoria-service", "atendimento-service",
  "triagem-service", "sinais-vitais-service", "evolucao-service", "anamnese-service",
  "diagnostico-service", "prescricao-service", "exames-service"
)

function Start-Modulo($Nome, $Pasta, $MvnCmd = "mvn spring-boot:run") {
  $pathPrefix = ""
  if ($MavenBin -ne "") { $pathPrefix = "`$env:PATH = '$MavenBin;' + `$env:PATH; " }
  $cmd = "$pathPrefix`$host.UI.RawUI.WindowTitle = '$Nome'; Set-Location '$Pasta'; $MvnCmd"
  Start-Process powershell -ArgumentList '-NoExit', '-Command', $cmd | Out-Null
  Write-Host "  iniciado: $Nome" -ForegroundColor Cyan
}

Write-Host "== 1/5 Eureka Server + Config Server ==" -ForegroundColor Yellow
foreach ($m in $Infra) { Start-Modulo $m (Join-Path $Root $m) }
Write-Host "Aguardando 20s para o Eureka/Config subirem..." -ForegroundColor DarkGray
Start-Sleep -Seconds 20

Write-Host "== 2/5 Microsservicos de negocio (15) ==" -ForegroundColor Yellow
foreach ($m in $Microservicos) { Start-Modulo $m (Join-Path $Root $m) }
Write-Host "Aguardando 15s antes do Gateway..." -ForegroundColor DarkGray
Start-Sleep -Seconds 15

Write-Host "== 3/5 API Gateway ==" -ForegroundColor Yellow
Start-Modulo "api-gateway" (Join-Path $Root "api-gateway")

if (-not $SemMonolito) {
  Write-Host "== 4/5 Monolito (rotas ainda nao extraidas) ==" -ForegroundColor Yellow
  Start-Modulo "monolito" $Root ".\mvnw.cmd spring-boot:run"
}

if (-not $SemFrontend) {
  Write-Host "== 5/5 Frontend ==" -ForegroundColor Yellow
  $webPath = Join-Path $Root "web"
  $cmdFront = "`$host.UI.RawUI.WindowTitle = 'web (frontend)'; Set-Location '$webPath'; npm run dev"
  Start-Process powershell -ArgumentList '-NoExit', '-Command', $cmdFront | Out-Null
  Write-Host "  iniciado: web (frontend)" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "Tudo iniciado. Cada servico abriu em sua propria janela de terminal." -ForegroundColor Green
Write-Host "Os servicos de negocio levam de 10 a 90s para registrar no Eureka." -ForegroundColor DarkGray
Write-Host ""
Write-Host "Eureka:   http://localhost:8761"
Write-Host "Gateway:  http://localhost:8000"
Write-Host "Frontend: http://localhost:5173"
Write-Host ""
Write-Host "Para derrubar tudo: scripts\stop-all.ps1" -ForegroundColor DarkGray
