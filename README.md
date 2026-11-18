# Auto Center FIAP — Microsserviço de Execução da Oficina

Microsserviço responsável pela **fila de serviços da oficina**: controla o diagnóstico, o
início e a finalização dos reparos. Faz parte do ecossistema de microsserviços do Tech
Challenge SOAT **Fase 4**.

- Faz parte do ecossistema do Tech Challenge SOAT **Fase 4**, com saga **orquestrada pelo
  serviço de Ordem de Serviço (OS Service)**.
- Ao receber `os.criada`, coloca a OS na fila e **conduz o diagnóstico**, publicando
  `diagnostico.iniciado` e, com os itens informados pela oficina, `diagnostico.concluido`.
- Ao receber `execucao.iniciar-reparo` (após o pagamento confirmado), inicia o reparo
  (`execucao.iniciada`).
- A oficina registra o resultado do diagnóstico, a finalização e a falha via **endpoints REST**.
- Ao finalizar publica `execucao.finalizada`; em falha, `execucao.falhou` (compensação).
  O evento `os.cancelada` cancela a execução.
- Usa **MongoDB (NoSQL)** para o histórico de etapas e a deduplicação de eventos.

---

## Arquitetura

Clean Architecture, com as mesmas camadas dos demais serviços do projeto:

```
domain/          # Entidades e regras de negócio puras (sem framework)
application/      # Casos de uso, DTOs, mappers e ports (in/out)
adapter/          # Entradas (REST, consumers Kafka) e saídas (Mongo, producers Kafka)
infrastructure/   # Configuração (Spring, Kafka, Mongo, observabilidade)
```

### Máquina de estados da execução

```
NA_FILA → EM_DIAGNOSTICO → DIAGNOSTICO_CONCLUIDO → EM_EXECUCAO → FINALIZADA
   └──────────┴────────────────────┴──────────────────┴───────→ FALHA | CANCELADA
```

---

## Saga Pattern

A Saga é **orquestrada pelo serviço de Ordem de Serviço (OS Service)**, que acompanha todo o
ciclo da OS e dispara as compensações. Os demais serviços — este incluso — participam de forma
coreografada: reagem a comandos/eventos e publicam eventos de resultado.

Todas as mensagens usam um **envelope padrão** (chave Kafka = `osId`):

```json
{ "eventId": "...", "tipo": "os.criada", "versao": 1, "osId": 123,
  "correlationId": "...", "ocorridoEm": "2026-01-01T00:00:00Z", "payload": {} }
```

- `eventId` deduplica mensagens repetidas (idempotência, coleção `eventos_processados`);
- valores monetários são strings com 2 casas (`"120.00"`);
- consumidores ignoram campos desconhecidos.

### Tópicos deste serviço

| Direção | Tópico | Tipos (`tipo`) |
|---------|--------|----------------|
| Consome | `os.eventos` | `os.criada`, `os.cancelada` |
| Consome | `execucao.comandos` | `execucao.iniciar-reparo` |
| Publica | `execucao.eventos` | `diagnostico.iniciado`, `diagnostico.concluido`, `execucao.iniciada`, `execucao.finalizada`, `execucao.falhou` |

Erro técnico tem 3 retentativas bloqueantes (1 s, 2 s, 4 s), que preservam a ordem das mensagens de cada OS; depois a mensagem vai para `<tópico>.dlt` (mesma partição: `os.eventos.dlt` e `execucao.comandos.dlt`). JSON inválido vai direto para a DLT.

### Fluxo principal

```
os.criada → (Execução) diagnostico.iniciado → diagnostico.concluido(itens)
  → OS gera orçamento/cobrança → pagamento.confirmado
  → OS envia execucao.iniciar-reparo → (Execução) execucao.iniciada → execucao.finalizada
```

Compensações: `execucao.falhou` (OS pede estorno) e `os.cancelada` (cancela a execução).

## API REST

- `GET  /execucoes/fila` — fila de execuções ativas.
- `GET  /execucoes/os/{osId}` — consulta a execução de uma OS.
- `POST /execucoes/{osId}/diagnostico` — conclui o diagnóstico com os itens (publica `diagnostico.concluido`).
- `POST /execucoes/{osId}/finalizacao` — finaliza o reparo (publica `execucao.finalizada`).
- `POST /execucoes/{osId}/falha` — registra falha (publica `execucao.falhou`).

O início do diagnóstico (em `os.criada`) e do reparo (em `execucao.iniciar-reparo`) são automáticos.

As rotas `/execucoes/**` exigem `Authorization: Bearer <jwt>` (HS256, issuer `Auto Center Fiap`, com `exp`), o mesmo token do OS Service. O segredo vem de `SISTEMA_SEGURANCA_CHAVE_SECRETA`, sem valor padrão.

No diagnóstico, cada serviço exige `servicoId`, `descricao` e `valor`, e cada produto exige `produtoId`, `descricao`, `quantidade` maior que zero e `precoUnitario`, como no contrato da Saga. Listas vazias são aceitas e levam o OS Service a cancelar a OS.

---

## Stack

- Java 21, Spring Boot 3.3.x
- Spring Data MongoDB (NoSQL)
- Spring for Apache Kafka
- MapStruct, Lombok
- springdoc-openapi (Swagger)
- Actuator + Micrometer/Prometheus + logs JSON (Datadog) — observabilidade da Fase 3
- Dashboards do Datadog versionados em [`/docs`](./docs/) — arquivos `.json` importáveis diretamente na plataforma Datadog
- Testes: JUnit, Testcontainers (Mongo + Kafka), Cucumber (BDD), JaCoCo (gate 80%)

---

## Como rodar localmente

Pré-requisitos: JDK 21, Docker.

```bash
# 1. Subir MongoDB + Kafka
make infra-up

# 2. Rodar a aplicação (profile dev)
make run
```

- API: http://localhost:8098
- Swagger UI: http://localhost:8098/swagger-ui.html
- Health: http://localhost:8098/actuator/health
- Métricas Prometheus: http://localhost:8098/actuator/prometheus

Variáveis de ambiente relevantes: `MONGODB_URI`, `KAFKA_BOOTSTRAP_SERVERS`, `SERVER_PORT`.

---

## Testes e cobertura

```bash
make test     # unitários + relatório de cobertura
make verify   # testes + gate de cobertura (mínimo 80%)
```

- **Unitários** (Surefire): domínio, entidade, máquina de estados, casos de uso, controllers, consumers e adapters.
- **Integração** (Testcontainers com MongoDB e Kafka reais): persistência e fluxo de mensagens.
- **BDD** (Cucumber): cenários de execução da OS, incluindo fluxo completo, cancelamento e consulta.
- **Gate:** o build falha com menos de 80% de cobertura de linhas (JaCoCo).

**Evidência** (`make verify`, 18/11/2026, BUILD SUCCESS):

| Métrica | Cobertas / total | % |
|---|---|---|
| Linhas | 414 / 428 | 96,7 |
| Branches | 48 / 63 | 76,2 |
| Instruções | 1838 / 1926 | 95,4 |
| Métodos | 128 / 133 | 96,2 |

Testes: 68, todos passando. Relatório HTML em `target/site/jacoco/index.html`, publicado como artefato `jacoco-report` em cada execução do pipeline.

---

## CI/CD e Kubernetes

Pipelines independentes em GitHub Actions (`.github/workflows/`):

- **`ci.yml`** — em PRs e pushes: build → testes (unitários + integração/BDD com Testcontainers) → qualidade (SonarCloud). É o check obrigatório para merge na `main` e na `homologacao`. Em push na `main` ou na `homologacao`, o job `deploy` roda depois do build e do Quality Gate, no runner self-hosted, com [`scripts/deploy-kind.sh`](scripts/deploy-kind.sh): build local, `kind load`, Secret a partir dos GitHub Secrets e `kubectl apply -k` com a imagem do commit. No cluster, o Kafka compartilhado fica no namespace `autocenter` (`kafka.autocenter.svc.cluster.local:9092`).
- **`cd.yml`** — em push na `main`: build e push da imagem para o **GHCR**.

Ambientes (os secrets do environment têm prioridade sobre os do repositório):

| Branch | Cluster kind | GitHub Environment |
|---|---|---|
| `main` | `autocenter` (`kind-autocenter`) | `producao` |
| `homologacao` | `autocenter-qa` (`kind-autocenter-qa`) | `homologacao` |

Manifestos Kubernetes e instruções de deploy em [`k8s/`](k8s/README.md).

### Proteção da branch `main`

Configurar em **Settings → Branches → Branch protection rules** para `main`:
- Require a pull request before merging (≥1 aprovação);
- Require status checks to pass → selecionar **`🔨 Build, Testes e Qualidade`** (job do `ci.yml`);
- Require branches to be up to date before merging.

### Secrets usados nos workflows

| Secret | Uso |
|--------|-----|
| `SONAR_TOKEN`, `SONAR_HOST_URL` | Análise SonarQube no CI (obrigatória: sem token o job falha) |
| `SISTEMA_SEGURANCA_CHAVE_SECRETA` | Segredo HS256 do JWT, o mesmo do OS Service (deploy) |
| `MONGO_INITDB_ROOT_USERNAME`, `MONGO_INITDB_ROOT_PASSWORD`, `MONGODB_URI` | Credenciais do MongoDB (deploy) |

---

## Status do desenvolvimento

- [x] Bootstrap do projeto (estrutura, build, config, observabilidade)
- [x] Domínio (Execucao, EventoOperacional, transições)
- [x] Casos de uso e ports
- [x] Persistência MongoDB
- [x] Consumers (`os.eventos`, `execucao.comandos`) + publisher (`execucao.eventos`) com envelope e dedup
- [x] Endpoints REST (fila, consulta, concluir diagnóstico, finalizar, falha)
- [x] Testes unitários + BDD + cobertura ≥80% (atual: 93,2%)
- [x] Dockerfile + manifestos Kubernetes
- [x] Pipeline CI/CD + Sonar
- [x] Migração para o novo contrato Kafka (saga orquestrada, tópicos consolidados)
