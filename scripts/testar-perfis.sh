#!/usr/bin/env bash
# ============================================================
# Testa os perfis de acesso do Prontuário Eletrônico.
# Pré-requisito: API rodando em http://localhost:8080 (perfil dev).
#   ./mvnw spring-boot:run
# Uso: bash scripts/testar-perfis.sh
# ============================================================
set -u
BASE="http://localhost:8080/api"
PASS=0; FAIL=0

login() { # $1=login $2=senha -> ecoa token
  curl -s -X POST "$BASE/auth/login" -H "Content-Type: application/json" \
    -d "{\"login\":\"$1\",\"senha\":\"$2\"}" | grep -o '"accessToken":"[^"]*"' | sed 's/.*:"//;s/"//'
}

# check METODO CAMINHO TOKEN ESPERADO DESCRICAO [BODY]
check() {
  local metodo="$1" caminho="$2" token="$3" esperado="$4" desc="$5" body="${6:-}"
  local args=(-s -o /dev/null -w "%{http_code}" -X "$metodo" "$BASE$caminho" -H "Authorization: Bearer $token")
  if [ -n "$body" ]; then args+=(-H "Content-Type: application/json" -d "$body"); fi
  local code; code=$(curl "${args[@]}")
  if [ "$code" = "$esperado" ]; then
    echo "  ✅ [$code] $desc"; PASS=$((PASS+1))
  else
    echo "  ❌ [$code, esperado $esperado] $desc"; FAIL=$((FAIL+1))
  fi
}

echo "=== Login ADMIN e preparação ==="
ADMIN=$(login admin admin123)
[ -z "$ADMIN" ] && { echo "Falha no login admin. A API está rodando?"; exit 1; }

# Cria 1 usuário por perfil (idempotente: ignora 409 se já existir)
criar_usuario() { curl -s -o /dev/null -X POST "$BASE/usuarios" -H "Authorization: Bearer $ADMIN" \
  -H "Content-Type: application/json" -d "$1"; }
criar_usuario '{"nomeCompleto":"Dra. Ana","login":"dra.ana","email":"ana@h.com","senha":"senha123","roles":["MEDICO"]}'
criar_usuario '{"nomeCompleto":"Enf. Bea","login":"enf.bea","email":"bea@h.com","senha":"senha123","roles":["ENFERMEIRO"]}'
criar_usuario '{"nomeCompleto":"Rec. Carla","login":"rec.carla","email":"carla@h.com","senha":"senha123","roles":["RECEPCAO"]}'
criar_usuario '{"nomeCompleto":"Fat. Diego","login":"fat.diego","email":"diego@h.com","senha":"senha123","roles":["FATURAMENTO"]}'

MED=$(login dra.ana senha123)
ENF=$(login enf.bea senha123)
REC=$(login rec.carla senha123)
FAT=$(login fat.diego senha123)

# Setor + medicamento + paciente base
SID=$(curl -s -X POST "$BASE/setores" -H "Authorization: Bearer $ADMIN" -H "Content-Type: application/json" \
  -d '{"nome":"Clinica Teste","tipo":"AMBULATORIO"}' | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://')
MEDID=$(curl -s -X POST "$BASE/medicamentos" -H "Authorization: Bearer $ADMIN" -H "Content-Type: application/json" \
  -d '{"nome":"Dipirona Teste","concentracao":"500mg","controlado":false}' | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://')
PID=$(curl -s -X POST "$BASE/pacientes" -H "Authorization: Bearer $REC" -H "Content-Type: application/json" \
  -d '{"nome":"Paciente Teste","cpf":"10120230340","dataNascimento":"1990-01-01"}' | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://')

# Nota: a validação do corpo (@Valid -> 400) ocorre ANTES da autorização (@PreAuthorize -> 403).
# Por isso os testes de "negação" enviam corpos VÁLIDOS, para isolar o 403.

echo
echo "=== ADMIN (TI: gerencia sistema, SEM acesso clínico) ==="
check POST "/setores" "$ADMIN" 201 "cria setor" '{"nome":"Setor Admin","tipo":"UTI"}'
check GET  "/auditoria" "$ADMIN" 200 "vê trilha de auditoria"
check GET  "/pacientes" "$ADMIN" 403 "NÃO acessa pacientes (sigilo)"

echo
echo "=== RECEPCAO (cadastro/atendimento, SEM clínico) ==="
check POST "/pacientes" "$REC" 201 "cadastra paciente" '{"nome":"Novo Pac","cpf":"55544433322","dataNascimento":"1995-05-05"}'
check POST "/atendimentos" "$REC" 201 "abre atendimento" "{\"pacienteId\":$PID,\"tipo\":\"AMBULATORIAL\",\"setorId\":$SID}"
check GET  "/auditoria" "$REC" 403 "NÃO vê auditoria"
check GET  "/dashboards/ocupacao" "$REC" 403 "NÃO vê dashboards"

echo
echo "=== MEDICO (prontuário completo) ==="
AID=$(curl -s -X POST "$BASE/atendimentos" -H "Authorization: Bearer $REC" -H "Content-Type: application/json" \
  -d "{\"pacienteId\":$PID,\"tipo\":\"AMBULATORIAL\",\"setorId\":$SID}" | grep -o '"id":[0-9]*' | head -1 | sed 's/"id"://')
check GET  "/pacientes" "$MED" 200 "lê pacientes"
check POST "/atendimentos/$AID/anamnese" "$MED" 201 "registra anamnese" '{"historiaDoencaAtual":"teste"}'
check POST "/setores" "$MED" 403 "NÃO cria setor (config é do ADMIN)" '{"nome":"Setor X Med","tipo":"UTI"}'

echo
echo "=== ENFERMEIRO (triagem/enfermagem) ==="
check POST "/atendimentos/$AID/triagem" "$ENF" 201 "faz triagem" '{"classificacaoRisco":"VERDE"}'
check POST "/atendimentos/$AID/sinais-vitais" "$ENF" 201 "registra sinais vitais" '{"frequenciaCardiaca":80}'
check POST "/atendimentos/$AID/prescricoes" "$ENF" 403 "NÃO prescreve" "{\"itens\":[{\"medicamentoId\":$MEDID,\"dose\":\"500mg\"}]}"

echo
echo "=== FATURAMENTO (financeiro/dashboards) ==="
check GET  "/dashboards/faturamento" "$FAT" 200 "vê dashboards"
check POST "/contas" "$FAT" 201 "abre conta" "{\"atendimentoId\":$AID}"
check POST "/pacientes" "$FAT" 403 "NÃO cadastra paciente" '{"nome":"X","cpf":"99988877766","dataNascimento":"1990-01-01"}'

echo
echo "============================================================"
echo "RESULTADO: $PASS ok, $FAIL falhas"
[ "$FAIL" -eq 0 ] && echo "✅ Todos os perfis se comportaram como esperado." || echo "⚠️  Há divergências acima."
