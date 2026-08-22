# ============================================================
# Derruba todos os processos da malha do Prontuario Eletronico,
# identificando-os pela porta TCP em escuta (nao depende de PID).
# Compativel com Windows PowerShell 5.1 e PowerShell 7+.
#
# Uso: powershell -ExecutionPolicy Bypass -File scripts\stop-all.ps1
# ============================================================
$Portas = @(
  8761, 8888, 8000, 8080,
  8081, 8082, 8083, 8085, 8086, 8087, 8088, 8089, 8090,
  8091, 8092, 8093, 8094, 8095, 8096,
  5173
)

$Parados = 0
foreach ($porta in $Portas) {
  $conexoes = Get-NetTCPConnection -LocalPort $porta -State Listen -ErrorAction SilentlyContinue
  foreach ($c in $conexoes) {
    $procId = $c.OwningProcess
    $proc = Get-Process -Id $procId -ErrorAction SilentlyContinue
    if ($proc) {
      Write-Host "  porta $porta -> encerrando PID $procId ($($proc.ProcessName))" -ForegroundColor Cyan
      Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
      $Parados++
    }
  }
}

if ($Parados -eq 0) {
  Write-Host "Nenhum processo encontrado nas portas conhecidas." -ForegroundColor DarkGray
} else {
  Write-Host "`n$Parados processo(s) encerrado(s)." -ForegroundColor Green
}
