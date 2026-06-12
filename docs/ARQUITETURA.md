# Prontuário Eletrônico — Documento de Arquitetura e Modelagem

> SOUL MV Hospitalar — Sistema de Prontuário Eletrônico
> Documento de design (Etapa 0). Aprovação necessária antes da implementação.
> Última atualização: 2026-06-09

---

## 1. Análise do sistema

### 1.1 Objetivo
Sistema web para gestão do prontuário eletrônico do paciente (PEP) em ambiente
hospitalar/clínico, cobrindo o ciclo de atendimento: **recepção → triagem →
atendimento clínico → prescrição → enfermagem → exames → faturamento**, com
portal de acesso ao próprio paciente.

### 1.2 Princípios diretores
- **Sigilo clínico (LGPD + ética médica):** o acesso ao conteúdo clínico é
  restrito por perfil. TI/ADMIN administra o sistema mas **não lê** prontuários;
  RECEPÇÃO não vê dados clínicos; FATURAMENTO vê apenas o necessário/anonimizado;
  PACIENTE só enxerga os próprios dados (isolamento total).
- **Trilha de auditoria imutável:** toda leitura/escrita de dado sensível gera
  log (quem, o quê, quando, de onde).
- **Camadas e responsabilidade única:** Controller (HTTP) → Service (regra) →
  Repository (persistência); DTOs na borda; entidades nunca expostas direto.
- **Segurança por padrão:** JWT, BCrypt, autorização por método (`@PreAuthorize`)
  e filtros de escopo de dados (data-scoping) no Service.

### 1.3 Atores / Perfis e matriz de permissões

| Recurso / Ação                          | ADMIN | MEDICO | ENFERMEIRO | RECEPCAO | FATURAMENTO | PACIENTE |
|-----------------------------------------|:-----:|:------:|:----------:|:--------:|:-----------:|:--------:|
| Gerenciar usuários e perfis             |  ✔    |   —    |     —      |    —     |      —      |    —     |
| Configurar setores/leitos/parâmetros    |  ✔    |   —    |     —      |    —     |      —      |    —     |
| Ver trilha de auditoria                 |  ✔    |   —    |     —      |    —     |      —      |    —     |
| Cadastro demográfico do paciente        |  —*   |   ✔    |     ✔      |    ✔     |      —      |    —     |
| Convênios / agendamento / fila          |  —    |   ✔    |     ✔      |    ✔     |      ✔      |   ✔(ag.) |
| Triagem / classificação de risco        |  —    |   ✔    |     ✔      |    —     |      —      |    —     |
| Sinais vitais                           |  —    |   ✔    |     ✔      |    —     |      —      |  ✔(ver)  |
| Anamnese / evolução / diagnóstico médico|  —    |   ✔    |   ver*     |    —     |      —      |  ✔(ver)  |
| Evolução de enfermagem                  |  —    |   ver  |     ✔      |    —     |      —      |    —     |
| Prescrição de medicamentos              |  —    |   ✔    |   ver/checar|   —     |      —      |  ✔(ver)  |
| Checagem/administração de medicação     |  —    |   ✔    |     ✔      |    —     |      —      |    —     |
| Solicitar exames                        |  —    |   ✔    |     —      |    —     |      —      |    —     |
| Lançar/ver resultados/laudos            |  —    |   ✔    |   ver      |    —     |   restrito  | ✔(próprio)|
| Dashboards gerenciais / ocupação        |  ✔(op)|   —    |     —      |    —     |      ✔      |    —     |
| Faturamento / guias TISS-TUSS           |  —    |   —    |     —      |    —     |      ✔      |    —     |

*Notas:* ADMIN **não** acessa conteúdo clínico (`—*`). ENFERMEIRO lê a evolução
médica mas não a altera (`ver*`). PACIENTE tem leitura limitada aos próprios
dados (data-scoping por `pacienteId == usuario.pacienteId`).

### 1.4 Requisitos não-funcionais
- Disponibilidade e integridade dos dados clínicos (transações ACID, PostgreSQL).
- Rastreabilidade (auditoria), conformidade LGPD.
- Paginação e filtros em todas as listagens.
- Documentação OpenAPI/Swagger.
- Containerização (Docker + Compose).
- Cobertura de testes em Services (regras) e endpoints críticos (integração).

---

## 2. Proposta de arquitetura

### 2.1 Stack
- **Backend:** Java 21, Spring Boot 3.3, Spring Security + JWT, Spring Data JPA,
  Bean Validation, MapStruct (mappers), springdoc-openapi (Swagger).
- **Banco:** PostgreSQL 16 (migrações com Flyway).
- **Frontend:** React + TypeScript + Vite, TailwindCSS, React Router, Axios,
  React Query (cache/estado server), React Hook Form + Zod (validação).
- **Infra:** Docker + Docker Compose (api, db, web).
- **Testes:** JUnit 5, Mockito, Spring Boot Test + Testcontainers (integração).

> Observação: o `pom.xml` atual usa Java 17 + H2 e não tem Security. A Etapa 1
> da implementação atualiza o `pom.xml` (Java 21, PostgreSQL, Security, Flyway,
> springdoc, MapStruct) e migra o `application.properties` para perfis
> (`dev` = H2/Postgres local, `prod` = Postgres container).

### 2.2 Camadas (backend)
```
HTTP  ── Controller  (validação de entrada, DTO, status HTTP, @PreAuthorize)
            │
Regra ── Service     (regra de negócio, transações, data-scoping, auditoria)
            │
Dados ── Repository  (Spring Data JPA, Specifications p/ filtros dinâmicos)
            │
         PostgreSQL
```
Transversais: `config`, `security` (JWT, filtros, RBAC), `exception` (handler
global `@RestControllerAdvice`), `audit` (AOP/listener), `mapper` (MapStruct).

### 2.3 Estrutura de pacotes (backend) — `com.soulmv.hospitalar`
```
com.soulmv.hospitalar
├── config           # OpenAPI, CORS, Jackson, beans
├── controller       # REST controllers (orquestração HTTP)
├── dto              # request/response DTOs (records)
│   ├── request
│   └── response
├── entity           # entidades JPA (substitui o atual `model`)
├── enums            # Role, StatusAtendimento, ClassificacaoRisco, etc.
├── exception        # exceções de domínio + GlobalExceptionHandler
├── repository       # Spring Data + Specifications
├── security         # JwtService, filtros, UserDetails, SecurityConfig
├── service          # regras de negócio
│   └── impl
├── mapper           # MapStruct
└── audit            # auditoria (entity listener / aspecto)
```
> O pacote atual `model` será renomeado para `entity` na migração.

### 2.4 Estrutura (frontend) — `web/`
```
web/src
├── components       # UI reutilizável (tabelas, forms, badges de risco)
├── pages            # telas (Login, Pacientes, Atendimento, Prescrição...)
├── services         # clients Axios por domínio (api.ts + *.service.ts)
├── routes           # rotas + guards por perfil (PrivateRoute, RoleRoute)
├── hooks            # useAuth, useDebounce, hooks de domínio
├── contexts         # AuthContext (token, usuário, perfis)
├── types            # tipos TS espelhando os DTOs
├── layouts          # AppLayout, AuthLayout, PortalPacienteLayout
└── utils            # formatadores (CPF, datas), constantes, helpers
```

---

## 3. Modelagem das entidades

Entidades principais (campos resumidos; `id`, `criadoEm`, `atualizadoEm`,
`criadoPor` em todas as auditáveis):

### Identidade & acesso
- **Usuario**: nome, login, email, senhaHash, ativo, `Set<Role>`,
  `profissional?` (FK), `paciente?` (FK p/ portal).
- **Role** (enum): ADMIN, MEDICO, ENFERMEIRO, RECEPCAO, FATURAMENTO, PACIENTE.
- **Profissional**: nome, tipoConselho (CRM/COREN), numeroConselho, ufConselho,
  especialidade, setor (FK).

### Cadastros / parâmetros (ADMIN)
- **Setor / Unidade**: nome, tipo (AMBULATORIO, EMERGENCIA, INTERNACAO, UTI),
  ativo.
- **Leito**: identificador, setor (FK), status (LIVRE, OCUPADO, MANUTENCAO,
  HIGIENIZACAO), atendimentoAtual? (FK).
- **Convenio**: nome, registroANS, tipo (PARTICULAR, PLANO, SUS).
- **Medicamento** (catálogo): nome, princípioAtivo, concentração, controlado(bool).
- **ProcedimentoTUSS**: codigoTUSS, descrição, valorReferencia.
- **CID10**: codigo, descrição.

### Paciente
- **Paciente**: nome, cpf (único), cartaoSus, dataNascimento, sexo, telefone,
  email, endereço (embutido), convenio (FK), numeroCarteirinha.

### Encontro clínico
- **Atendimento (Encontro)**: paciente (FK), tipo (AMBULATORIAL, EMERGENCIA,
  INTERNACAO), status (AGENDADO, EM_TRIAGEM, EM_ATENDIMENTO, INTERNADO,
  AGUARDANDO_EXAME, ALTA, CANCELADO), profissionalResponsavel (FK), setor (FK),
  leito? (FK), dataEntrada, dataAlta, motivo/queixa.
- **Triagem**: atendimento (FK 1-1), enfermeiro (FK), queixaPrincipal,
  classificacaoRisco (VERMELHO, LARANJA, AMARELO, VERDE, AZUL — Manchester),
  dataHora.
- **SinaisVitais**: atendimento (FK), pa Sistólica/Diastólica, fc, fr,
  temperatura, satO2, glicemia, escalaDor, registradoPor (FK), dataHora.
- **EvolucaoClinica**: atendimento (FK), tipo (MEDICA, ENFERMAGEM),
  autor (FK Profissional), texto, dataHora, assinada(bool). Regra: ENFERMEIRO
  não edita evolução MEDICA.
- **Anamnese**: atendimento (FK), historiaDoenca, antecedentes, alergias,
  exameFisico, autor (FK), dataHora.
- **Diagnostico**: atendimento (FK), cid10 (FK), tipo (PRINCIPAL, SECUNDARIO),
  medico (FK).

### Prescrição & enfermagem
- **Prescricao**: atendimento (FK), medico (FK), dataHora, status (ATIVA,
  SUSPENSA, ENCERRADA), `List<ItemPrescricao>`.
- **ItemPrescricao**: prescricao (FK), medicamento (FK), dose, via, frequencia,
  duracao, observacao.
- **AdministracaoMedicamento** (checagem): itemPrescricao (FK), enfermeiro (FK),
  dataHoraPrevista, dataHoraAdministracao, status (PENDENTE, ADMINISTRADO,
  RECUSADO, NAO_ADMINISTRADO), observacao.

### Exames
- **SolicitacaoExame**: atendimento (FK), medicoSolicitante (FK), tipoExame,
  status (SOLICITADO, COLETADO, EM_ANALISE, LIBERADO), dataSolicitacao.
- **ResultadoExame / Laudo**: solicitacao (FK 1-1), resultadoTexto, laudoArquivo
  (FK Anexo, PDF), liberadoPor (FK), dataLiberacao.

### Agendamento, faturamento, anexos, auditoria
- **Agendamento**: paciente (FK), tipo (CONSULTA, EXAME), profissional? (FK),
  dataHora, status (AGENDADO, CONFIRMADO, REALIZADO, CANCELADO, FALTOU).
- **ContaHospitalar**: atendimento (FK 1-1), convenio (FK), status (ABERTA,
  FECHADA, FATURADA, GLOSADA), valorTotal, `List<ItemConta>`.
- **ItemConta**: conta (FK), procedimentoTUSS (FK), quantidade, valorUnitario.
- **GuiaTISS**: conta (FK), numeroGuia, status, dadosTISS (export).
- **Anexo**: nomeArquivo, contentType, tamanho, caminho/blob, referência
  (entidade+id), enviadoPor.
- **LogAuditoria**: usuario (FK), acao, entidade, entidadeId, dadosAntes/depois
  (JSON), ip, dataHora. **Append-only** (sem update/delete).

---

## 4. Relacionamentos do banco

```
Usuario *──* Role           (usuario_roles)
Usuario 1──0..1 Profissional
Usuario 1──0..1 Paciente    (vínculo portal)
Profissional *──1 Setor

Paciente 1──* Atendimento
Paciente *──1 Convenio
Paciente 1──* Agendamento

Atendimento *──1 Setor
Atendimento 0..1──1 Leito
Atendimento 1──0..1 Triagem
Atendimento 1──* SinaisVitais
Atendimento 1──* EvolucaoClinica
Atendimento 1──0..1 Anamnese
Atendimento 1──* Diagnostico         Diagnostico *──1 CID10
Atendimento 1──* Prescricao          Prescricao 1──* ItemPrescricao
ItemPrescricao *──1 Medicamento
ItemPrescricao 1──* AdministracaoMedicamento
Atendimento 1──* SolicitacaoExame    SolicitacaoExame 1──0..1 ResultadoExame
Atendimento 1──0..1 ContaHospitalar  ContaHospitalar 1──* ItemConta
ItemConta *──1 ProcedimentoTUSS      ContaHospitalar 1──* GuiaTISS
Leito *──1 Setor

(qualquer entidade) 1──* Anexo  (polimórfico por referência)
Usuario 1──* LogAuditoria
```

Índices-chave: `paciente.cpf` (único), `atendimento(paciente_id, status)`,
`administracao(status, data_hora_prevista)`, `log_auditoria(usuario_id, data)`.

---

## 5. Lista de endpoints (REST `/api`)

> Todos exigem JWT exceto `/auth/login`. Autorização por perfil indicada.

**Auth**
- `POST /auth/login` · `POST /auth/refresh` · `GET /auth/me`

**Usuários (ADMIN)**
- `GET /usuarios` · `POST /usuarios` · `GET /usuarios/{id}` ·
  `PUT /usuarios/{id}` · `PATCH /usuarios/{id}/status` ·
  `PATCH /usuarios/{id}/roles`

**Parâmetros (ADMIN)**
- `CRUD /setores` · `CRUD /leitos` (+ `PATCH /leitos/{id}/status`) ·
  `CRUD /convenios` · `CRUD /medicamentos` · `CRUD /procedimentos-tuss`

**Pacientes (RECEPCAO, MEDICO, ENFERMEIRO)**
- `GET /pacientes` (filtros: nome, cpf, convênio; paginado) ·
  `POST /pacientes` · `GET /pacientes/{id}` · `PUT /pacientes/{id}`

**Atendimentos**
- `GET /atendimentos` (filtros: status, setor, data, paciente) ·
  `POST /atendimentos` (RECEPCAO) · `GET /atendimentos/{id}` ·
  `PATCH /atendimentos/{id}/status` · `POST /atendimentos/{id}/alta` (MEDICO)

**Triagem & enfermagem (ENFERMEIRO)**
- `POST /atendimentos/{id}/triagem` · `POST /atendimentos/{id}/sinais-vitais` ·
  `GET /atendimentos/{id}/sinais-vitais` ·
  `POST /atendimentos/{id}/evolucoes` (tipo=ENFERMAGEM) ·
  `POST /administracoes/{id}/checar`

**Clínico (MEDICO)**
- `POST /atendimentos/{id}/anamnese` ·
  `POST /atendimentos/{id}/evolucoes` (tipo=MEDICA) ·
  `POST /atendimentos/{id}/diagnosticos` ·
  `POST /atendimentos/{id}/prescricoes` ·
  `PATCH /prescricoes/{id}/status` ·
  `POST /atendimentos/{id}/exames` (solicitar) ·
  `PATCH /exames/{id}/resultado` (liberar laudo)

**Agendamento (RECEPCAO, PACIENTE p/ próprio)**
- `CRUD /agendamentos` (filtros por profissional, data, status)

**Faturamento (FATURAMENTO)**
- `GET /contas` · `GET /contas/{id}` · `POST /contas/{id}/fechar` ·
  `POST /contas/{id}/itens` · `POST /contas/{id}/guias-tiss` (export TISS)

**Dashboards (FATURAMENTO, ADMIN)**
- `GET /dashboards/ocupacao` · `GET /dashboards/atendimentos` ·
  `GET /dashboards/faturamento`

**Auditoria (ADMIN)**
- `GET /auditoria` (filtros: usuário, entidade, período)

**Portal do paciente (PACIENTE — sempre data-scoped ao próprio id)**
- `GET /portal/meus-atendimentos` · `GET /portal/meus-exames` ·
  `GET /portal/exames/{id}/laudo` (download PDF) · `POST /portal/agendamentos`

---

## 6. Fluxo das principais telas

1. **Login** → token JWT → redireciona conforme perfil (staff → Dashboard
   clínico/operacional; PACIENTE → Portal).
2. **Recepção:** busca/cadastro de paciente → abre Atendimento → entra na fila.
3. **Triagem (enfermagem):** lista de espera → classificação de risco
   (Manchester, badge colorido) + sinais vitais → encaminha.
4. **Atendimento médico:** painel do paciente (linha do tempo) → anamnese →
   evolução → diagnóstico (CID-10) → prescrição → solicitação de exames → alta.
5. **Enfermagem (beira-leito):** prescrições ativas → checagem de medicação →
   evolução de enfermagem → sinais vitais.
6. **Exames:** acompanhamento de status → liberação de resultado/laudo (upload).
7. **Faturamento:** contas por atendimento → itens (TUSS) → fechar → gerar guia
   TISS → dashboards (ocupação de leitos, contas).
8. **Admin:** usuários/perfis, setores/leitos, catálogos, trilha de auditoria.
9. **Portal do paciente:** meus atendimentos, meus exames (download PDF),
   agendamento online — tudo restrito aos próprios dados.

Telas mínimas: Login, Dashboard, Lista/Cadastro/Detalhe de Paciente, Fila de
atendimento, Triagem, Prontuário do atendimento (timeline), Prescrição,
Checagem de medicação, Exames/Laudos, Agendamento, Contas/Faturamento,
Dashboards, Usuários, Setores/Leitos, Auditoria, Portal do paciente.

---

## 7. Estratégia de segurança, sigilo clínico e auditoria
> (substitui a "estratégia de produtividade" — não aplicável a um PEP)

- **Autenticação:** login emite *access token* (curto) + *refresh token*; senhas
  com BCrypt; `JwtAuthenticationFilter` popula o `SecurityContext`.
- **Autorização em 2 níveis:**
  1. *Por papel* — `@PreAuthorize("hasRole('MEDICO')")` nos endpoints.
  2. *Por escopo de dado (data-scoping)* — no Service: PACIENTE só acessa
     registros onde `paciente.id == usuarioLogado.pacienteId`; FATURAMENTO recebe
     DTOs sem conteúdo clínico detalhado (projeção/anonimização).
- **Sigilo do ADMIN:** controllers clínicos não concedem ROLE_ADMIN; mesmo com
  acesso de infra, ADMIN não tem endpoint para ler evolução/diagnóstico.
- **Auditoria:** `@EntityListeners` + aspecto registram CRUD de entidades
  sensíveis e **toda leitura** de prontuário em `LogAuditoria` (append-only).
- **LGPD:** minimização de dados nos DTOs por perfil, trilha de acesso,
  finalidade explícita; export de paciente sob demanda (portabilidade).
- **Exceções:** `GlobalExceptionHandler` padroniza respostas (RFC 7807 problem
  details) sem vazar stack trace.

---

## 8. Ordem de implementação proposta
1. **Fundação:** atualizar `pom.xml` (Java 21, Postgres, Security, Flyway,
   springdoc, MapStruct), perfis de configuração, `GlobalExceptionHandler`,
   OpenAPI, Docker Compose (db).
2. **Autenticação & usuários:** Usuario/Role, JWT, BCrypt, `/auth/*`, `/usuarios`.
3. **Parâmetros (ADMIN):** setores, leitos, convênios, catálogos.
4. **Pacientes:** migrar/expandir o CRUD atual + filtros/paginação.
5. **Atendimentos:** encontro, status, leitos, fila.
6. **Triagem & enfermagem:** classificação de risco, sinais vitais, checagem.
7. **Clínico:** anamnese, evolução, diagnóstico, prescrição, exames/laudos.
8. **Faturamento & dashboards:** contas, TUSS/TISS, indicadores.
9. **Auditoria/LGPD:** trilha completa.
10. **Frontend** (por módulo, na mesma ordem dos domínios).
11. **Testes** (unitários de Service + integração com Testcontainers).
12. **Docker Compose completo + README final.**

---

### Pendências para confirmação antes de codar
- Manter o `groupId`/pacote `com.soulmv.hospitalar`? (sim, recomendado)
- Banco em dev: usar **PostgreSQL via Docker** desde já, ou **H2** para começar
  rápido e migrar depois?
- Profundidade do faturamento TISS/TUSS: **completo** ou **MVP** (conta + itens
  + status, sem geração XML TISS) nesta primeira versão?
- Armazenamento de anexos/laudos: **filesystem/volume** ou **bytea no banco** ou
  **MinIO/S3**?
