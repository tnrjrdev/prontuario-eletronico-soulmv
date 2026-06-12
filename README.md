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

Backend em **camadas** (Controller → Service → Repository) com DTOs na borda,
MapStruct para conversão, tratamento global de exceções (RFC 7807), segurança
**JWT stateless** e autorização por perfil (`@PreAuthorize`) + escopo de dados.

```
Browser ─► React (Vite/Nginx) ─► /api ─► Spring Boot ─► PostgreSQL
                                          │
                                          ├─ Security (JWT, RBAC)
                                          ├─ Auditoria (interceptor append-only)
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

**Backend** (perfil `dev` = H2 em memória, sem setup de banco):
```bash
./mvnw spring-boot:run          # Linux/Mac
.\mvnw.cmd spring-boot:run       # Windows
```
- Swagger: http://localhost:8080/swagger-ui.html
- Console H2: http://localhost:8080/h2-console (JDBC `jdbc:h2:mem:hospitalardb`, user `sa`, sem senha)

**Frontend** (proxy `/api` → 8080):
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
