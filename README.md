# SOUL MV Hospitalar — Prontuário Eletrônico (PEP)

Sistema web de prontuário eletrônico do paciente, cobrindo o ciclo assistencial
**recepção → triagem → atendimento clínico → prescrição → enfermagem → exames →
faturamento**, com trilha de auditoria/LGPD e portais por perfil.

> Backend Java 21 / Spring Boot 3 · Frontend React + TypeScript + Vite ·
> PostgreSQL · Docker.

---

## Sumário
- [Arquitetura](#arquitetura)
- [Stack](#stack)
- [Perfis de acesso](#perfis-de-acesso)
- [Como rodar](#como-rodar)
  - [Opção A — Docker (tudo de uma vez)](#opção-a--docker-tudo-de-uma-vez)
  - [Opção B — Desenvolvimento local](#opção-b--desenvolvimento-local)
- [Credenciais iniciais](#credenciais-iniciais)
- [Principais endpoints](#principais-endpoints)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Testes](#testes)
- [Documentação de design](#documentação-de-design)
- [Melhorias futuras](#melhorias-futuras)

---

## Arquitetura

Backend em migração incremental (*strangler fig*) de monólito para
**microsserviços**: Eureka (descoberta), Config Server, API Gateway e 15
serviços de negócio já extraídos, com o monólito ainda respondendo pelas
poucas rotas não migradas. Cada serviço segue **camadas** (Controller →
Service → Repository) com DTOs na borda, MapStruct para conversão,
tratamento global de exceções (RFC 7807), segurança **JWT stateless** e
autorização por perfil (`@PreAuthorize`); chamadas entre serviços usam
OpenFeign + Resilience4j (circuit breaker).

```
Browser ─► React (Vite/Nginx) ─► API Gateway (:8000) ─┬─► microsserviços (Eureka) ─► H2/PostgreSQL
                                                         └─► Monólito (:8080, rotas remanescentes)
                                          │
                                          ├─ Security (JWT, RBAC) — em cada serviço
                                          ├─ Auditoria (auditoria-service, append-only)
                                          └─ Storage (laudos em filesystem/volume)
```

## Stack

| Camada | Tecnologias |
|---|---|
| Backend | Java 21, Spring Boot 3.3, Spring Security + JWT (jjwt), Spring Data JPA, Bean Validation, MapStruct, springdoc-openapi |
| Banco | PostgreSQL 16 (prod) · H2 em memória (dev) |
| Frontend | React 18, TypeScript, Vite, TailwindCSS, React Router, Axios |
| Testes | JUnit 5, Mockito, Spring Security Test, Testcontainers (disponível) |
| Infra | Docker, Docker Compose, Nginx |

## Perfis de acesso

| Perfil | Acesso principal | Restrição-chave |
|---|---|---|
| **ADMIN** | Usuários, parâmetros (setores/leitos/catálogos), auditoria | **Sem acesso ao conteúdo clínico** |
| **MEDICO** | Anamnese, evolução, diagnóstico, prescrição, exames/laudos | — |
| **ENFERMEIRO** | Triagem, sinais vitais, evolução de enfermagem, checagem de medicação | Não prescreve; não altera evolução médica |
| **RECEPCAO** | Cadastro de pacientes, abertura de atendimento | **Sem acesso clínico** |
| **FATURAMENTO** | Contas, TUSS, guias TISS, dashboards | Clínico restrito ao necessário |
| **PACIENTE** | (Portal — previsto) próprios dados | Isolamento total |

---

## Como rodar

### Opção A — Docker (tudo de uma vez)

Pré-requisito: Docker + Docker Compose.

```bash
docker compose up -d --build
```

| Serviço | URL |
|---|---|
| Frontend | http://localhost:3000 |
| API + Swagger | http://localhost:8080/swagger-ui.html |
| Adminer (DB) | http://localhost:8081 |

Parar: `docker compose down` (use `-v` para apagar os volumes de dados).

### Opção B — Desenvolvimento local

O backend hoje é uma **malha de microsserviços** (Eureka + Config Server +
Gateway + 15 serviços de negócio), com o antigo monólito ainda de pé como
destino padrão do Gateway para as rotas que não foram extraídas. Todos
compartilham o mesmo arquivo H2 (`db/hospitalar-v2`) em modo dev.

Pré-requisitos: **Java 21**, **Maven** (`mvn` no PATH — veja nota abaixo) e
**Node 18+**.

#### Opção B.1 — Script (recomendado no Windows)

> Execute a partir da **raiz do repositório** (onde este README está) — o
> caminho `scripts\start-all.ps1` é relativo. Se estiver em outra pasta, use
> `cd F:\Dev\Prontuario_eletronico` antes, ou o caminho completo do script.

```powershell
powershell -ExecutionPolicy Bypass -File scripts\start-all.ps1
```

Abre uma janela de terminal por processo, na ordem certa (Eureka/Config →
microsserviços → Gateway → Monólito → Frontend). Flags úteis:

```powershell
# sem o monólito e sem o frontend
powershell -ExecutionPolicy Bypass -File scripts\start-all.ps1 -SemMonolito -SemFrontend

# se "mvn" não for reconhecido (adiciona o Maven ao PATH só dentro do script)
# exemplo válido nesta máquina:
powershell -ExecutionPolicy Bypass -File scripts\start-all.ps1 -MavenBin "C:\Users\taryj\apache-maven-3.9.6\bin"
```

Para derrubar tudo de novo (identifica os processos pela porta, não pelo PID):
```powershell
powershell -ExecutionPolicy Bypass -File scripts\stop-all.ps1
```

#### Opção B.2 — Manual, um serviço por vez

Se `mvn` não estiver no PATH da sessão (erro *"não é reconhecido..."*), defina
antes de cada comando (PowerShell não usa `export` nem `&&`):
```powershell
$env:PATH = "C:\caminho\para\maven\bin;$env:PATH"
```

Cada linha abaixo roda em **primeiro plano** — abra uma aba/janela de
terminal nova para cada serviço, na ordem:

```powershell
# 1) Descoberta e configuração (primeiro; os demais dependem deles)
cd eureka-server;  mvn spring-boot:run    # :8761
cd config-server;  mvn spring-boot:run    # :8888

# 2) Microsserviços de negócio (qualquer ordem entre si)
cd iam-service;            mvn spring-boot:run   # :8081
cd paciente-service;       mvn spring-boot:run   # :8082
cd catalogo-service;       mvn spring-boot:run   # :8083
cd agendamento-service;    mvn spring-boot:run   # :8085
cd faturamento-service;    mvn spring-boot:run   # :8086
cd dashboard-service;      mvn spring-boot:run   # :8087
cd auditoria-service;      mvn spring-boot:run   # :8088
cd atendimento-service;    mvn spring-boot:run   # :8089
cd triagem-service;        mvn spring-boot:run   # :8090
cd sinais-vitais-service;  mvn spring-boot:run   # :8091
cd evolucao-service;       mvn spring-boot:run   # :8092
cd anamnese-service;       mvn spring-boot:run   # :8093
cd diagnostico-service;    mvn spring-boot:run   # :8094
cd prescricao-service;     mvn spring-boot:run   # :8095
cd exames-service;         mvn spring-boot:run   # :8096

# 3) Gateway (depois dos serviços, para rotear certo)
cd api-gateway;    mvn spring-boot:run    # :8000

# 4) Monólito (ainda é o destino padrão do Gateway para rotas não extraídas)
.\mvnw.cmd spring-boot:run                # :8080
```

Cada serviço leva de 10 a 90 segundos para subir e se registrar no Eureka
(painel em http://localhost:8761). Swagger de cada serviço fica em
`http://localhost:<porta>/swagger-ui.html`; pelo Gateway, tudo é acessado via
`http://localhost:8000/api/...`.

**Frontend** (proxy `/api` → Gateway, porta 8000):
```bash
cd web
npm install
npm run dev                      # http://localhost:5173
```

---

## Credenciais iniciais

Um usuário **ADMIN** é criado automaticamente na primeira execução
(`DataSeeder`):

```
login: admin
senha: admin123
```

> Em produção, defina `ADMIN_SENHA` (e `JWT_SECRET`) por variável de ambiente.

---

## Principais endpoints

| Área | Rotas |
|---|---|
| Auth | `POST /api/auth/login` · `POST /api/auth/refresh` · `GET /api/auth/me` |
| Usuários (ADMIN) | `GET/POST /api/usuarios` · `PATCH /api/usuarios/{id}/status|roles` |
| Parâmetros (ADMIN) | `/api/setores` · `/api/leitos` · `/api/convenios` · `/api/medicamentos` · `/api/procedimentos-tuss` · `/api/cid10` |
| Pacientes | `GET/POST/PUT /api/pacientes` (filtros nome/cpf/convênio) |
| Atendimentos | `/api/atendimentos` (fila, status, `/leito`, `/alta`) |
| Triagem/Enfermagem | `/api/atendimentos/{id}/triagem` · `/sinais-vitais` · `/evolucoes` |
| Clínico | `/anamnese` · `/diagnosticos` · `/prescricoes` · `/itens-prescricao/{id}/administracoes` · `/exames` (+ `/resultado`, `/laudo`) |
| Faturamento | `/api/contas` (itens, fechar, `guias-tiss`) · `GET /api/guias-tiss/{id}/xml` |
| Dashboards | `/api/dashboards/ocupacao|atendimentos|faturamento` |
| Auditoria (ADMIN) | `GET /api/auditoria` |

A referência completa e interativa está no **Swagger UI**.

---

## Estrutura do projeto

```
.
├── src/main/java/com/soulmv/hospitalar
│   ├── config        # OpenAPI, Security, JPA, seed, auditoria (interceptor)
│   ├── controller    # REST controllers
│   ├── dto           # request/response (records)
│   ├── entity        # entidades JPA
│   ├── enums         # Role, status, classificações
│   ├── exception     # exceções + handler global (RFC 7807)
│   ├── mapper        # MapStruct
│   ├── repository    # Spring Data + Specifications
│   ├── security      # JWT (service, filtro, userdetails)
│   └── service       # regras de negócio (+ storage, faturamento, support)
├── src/test/java     # testes unitários e de integração
├── web               # frontend React + TS + Vite
│   └── src/{components,pages,services,routes,hooks,contexts,types,layouts,utils}
├── docs/ARQUITETURA.md
├── Dockerfile        # backend
├── docker-compose.yml
└── README.md
```

---

## Testes

```bash
./mvnw test            # backend (unitários + integração)
cd web && npm run build # frontend (type-check + build)
```

- Unitários (Mockito): regras de `UsuarioService`, `AtendimentoService`, `ContaService`.
- Integração (MockMvc + Spring Security, H2): fluxo de login/JWT e RBAC.
- Testcontainers está disponível para testes de fidelidade com PostgreSQL.

---

## Documentação de design

O documento de arquitetura e modelagem (entidades, relacionamentos, endpoints,
fluxo de telas e estratégia de sigilo/LGPD) está em
[`docs/ARQUITETURA.md`](docs/ARQUITETURA.md).

---

## Melhorias futuras

- Migrações de schema com **Flyway** (substituir `ddl-auto`).
- Portal do paciente (vínculo `Usuario`↔`Paciente`, isolamento total).
- Telas clínicas detalhadas no frontend (prontuário do atendimento).
- TISS completo no padrão XSD oficial da ANS (cabeçalho/hash/epílogo).
- Storage em MinIO/S3; rate limiting no login; refresh token com rotação.
- Auditoria com diff antes/depois (AOP) e exportação assinada (LGPD).
```
