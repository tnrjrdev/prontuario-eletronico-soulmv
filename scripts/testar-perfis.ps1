# ============================================================
# Testa os perfis de acesso do Prontuario Eletronico.
# Compativel com Windows PowerShell 5.1 e PowerShell 7+.
# Pre-requisito: API rodando em http://localhost:8080 (perfil dev, banco H2 LIMPO).
#   .\mvnw.cmd spring-boot:run
# Uso: powershell -ExecutionPolicy Bypass -File scripts\testar-perfis.ps1
# ============================================================
$Base = "http://localhost:8000/api"
$script:Pass = 0
$script:Fail = 0

function Login($login, $senha) {
  try {
    $r = Invoke-RestMethod -Uri "$Base/auth/login" -Method Post -ContentType "application/json" `
      -Body (@{ login = $login; senha = $senha } | ConvertTo-Json)
    return $r.accessToken
  } catch { return $null }
}

# Check -Metodo -Caminho -Token -Esperado -Desc [-Body]
function Check($Metodo, $Caminho, $Token, $Esperado, $Desc, $Body) {
  $headers = @{ Authorization = "Bearer $Token" }
  try {
    if ($Body) {
      $resp = Invoke-WebRequest -Uri "$Base$Caminho" -Method $Metodo -Headers $headers `
        -ContentType "application/json" -Body $Body -UseBasicParsing
    } else {
      $resp = Invoke-WebRequest -Uri "$Base$Caminho" -Method $Metodo -Headers $headers -UseBasicParsing
    }
    $code = [int]$resp.StatusCode
  } catch {
    if ($_.Exception.Response) { $code = [int]$_.Exception.Response.StatusCode.value__ } else { $code = -1 }
  }
  if ($code -eq $Esperado) {
    Write-Host "  [OK $code] $Desc" -ForegroundColor Green; $script:Pass++
  } else {
    Write-Host "  [FALHA $code, esperado $Esperado] $Desc" -ForegroundColor Red; $script:Fail++
  }
}

function NovoId($Caminho, $Token, $Body) {
  $headers = @{ Authorization = "Bearer $Token" }
  try {
    $r = Invoke-RestMethod -Uri "$Base$Caminho" -Method Post -Headers $headers `
      -ContentType "application/json" -Body $Body
    return $r.id
  } catch { return $null }
}

Write-Host "=== Login ADMIN e preparacao ==="
$ADMIN = Login "admin" "admin123"
if (-not $ADMIN) { Write-Host "Falha no login admin. A API esta rodando?" -ForegroundColor Red; exit 1 }

function CriarUsuario($Body) {
  try { Invoke-RestMethod -Uri "$Base/usuarios" -Method Post -Headers @{ Authorization = "Bearer $ADMIN" } `
    -ContentType "application/json" -Body $Body | Out-Null } catch {}
}
CriarUsuario('{"nomeCompleto":"Dra. Ana","login":"dra.ana","email":"ana@h.com","senha":"senha123","roles":["MEDICO"]}')
CriarUsuario('{"nomeCompleto":"Enf. Bea","login":"enf.bea","email":"bea@h.com","senha":"senha123","roles":["ENFERMEIRO"]}')
CriarUsuario('{"nomeCompleto":"Rec. Carla","login":"rec.carla","email":"carla@h.com","senha":"senha123","roles":["RECEPCAO"]}')
CriarUsuario('{"nomeCompleto":"Fat. Diego","login":"fat.diego","email":"diego@h.com","senha":"senha123","roles":["FATURAMENTO"]}')

$MED = Login "dra.ana" "senha123"
$ENF = Login "enf.bea" "senha123"
$REC = Login "rec.carla" "senha123"
$FAT = Login "fat.diego" "senha123"

# OBS: nao use $PID (reservado pelo PowerShell). Usamos $PacId.
$SID   = NovoId "/setores" $ADMIN '{"nome":"Clinica Teste","tipo":"AMBULATORIO"}'
$MEDID = NovoId "/medicamentos" $ADMIN '{"nome":"Dipirona Teste","concentracao":"500mg","controlado":false}'
$PacId = NovoId "/pacientes" $REC '{"nome":"Paciente Teste","cpf":"10120230340","dataNascimento":"1990-01-01"}'
$AID   = NovoId "/atendimentos" $REC "{`"pacienteId`":$PacId,`"tipo`":`"AMBULATORIAL`",`"setorId`":$SID}"

# Nota: a validacao do corpo (@Valid -> 400) ocorre ANTES da autorizacao (@PreAuthorize -> 403);
# por isso os testes de negacao enviam corpos VALIDOS para isolar o 403.

Write-Host "`n=== ADMIN (TI: gerencia sistema, SEM acesso clinico) ==="
Check "Post" "/setores" $ADMIN 201 "cria setor" '{"nome":"Setor Admin","tipo":"UTI"}'
Check "Get"  "/auditoria" $ADMIN 200 "ve trilha de auditoria"
Check "Get"  "/pacientes" $ADMIN 403 "NAO acessa pacientes (sigilo)"

Write-Host "`n=== RECEPCAO (cadastro/atendimento, SEM clinico) ==="
Check "Post" "/pacientes" $REC 201 "cadastra paciente" '{"nome":"Novo Pac","cpf":"55544433322","dataNascimento":"1995-05-05"}'
Check "Post" "/atendimentos" $REC 201 "abre atendimento" "{`"pacienteId`":$PacId,`"tipo`":`"AMBULATORIAL`",`"setorId`":$SID}"
Check "Get"  "/auditoria" $REC 403 "NAO ve auditoria"
Check "Get"  "/dashboards/ocupacao" $REC 403 "NAO ve dashboards"

Write-Host "`n=== MEDICO (prontuario completo) ==="
Check "Get"  "/pacientes" $MED 200 "le pacientes"
Check "Post" "/atendimentos/$AID/anamnese" $MED 201 "registra anamnese" '{"historiaDoencaAtual":"teste"}'
Check "Post" "/setores" $MED 403 "NAO cria setor" '{"nome":"Setor X Med","tipo":"UTI"}'

Write-Host "`n=== ENFERMEIRO (triagem/enfermagem) ==="
Check "Post" "/atendimentos/$AID/triagem" $ENF 201 "faz triagem" '{"classificacaoRisco":"VERDE"}'
Check "Post" "/atendimentos/$AID/sinais-vitais" $ENF 201 "registra sinais vitais" '{"frequenciaCardiaca":80}'
Check "Post" "/atendimentos/$AID/prescricoes" $ENF 403 "NAO prescreve" "{`"itens`":[{`"medicamentoId`":$MEDID,`"dose`":`"500mg`"}]}"

Write-Host "`n=== FATURAMENTO (financeiro/dashboards) ==="
Check "Get"  "/dashboards/faturamento" $FAT 200 "ve dashboards"
Check "Post" "/contas" $FAT 201 "abre conta" "{`"atendimentoId`":$AID}"
Check "Post" "/pacientes" $FAT 403 "NAO cadastra paciente" '{"nome":"X","cpf":"99988877766","dataNascimento":"1990-01-01"}'

Write-Host "`n============================================================"
Write-Host "RESULTADO: $script:Pass ok, $script:Fail falhas"
if ($script:Fail -eq 0) { Write-Host "Todos os perfis se comportaram como esperado." -ForegroundColor Green }
else { Write-Host "Ha divergencias acima." -ForegroundColor Yellow }
