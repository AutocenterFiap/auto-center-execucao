# Auto Center FIAP — Microsserviço de Execução da Oficina

Microsserviço responsável pela **fila de serviços da oficina**: controla o diagnóstico, o
início e a finalização dos reparos. Faz parte do ecossistema de microsserviços do Tech
Challenge SOAT **Fase 4**.

- Consome o evento `pagamento.confirmado` (do serviço de Orçamento e Pagamento) e coloca a
  Ordem de Serviço (OS) na fila de execução.
- As etapas (diagnóstico, reparo, finalização) são avançadas via **endpoints REST** pela
  oficina.
- Ao finalizar, publica `execucao.finalizada` para o serviço de Ordem de Serviço concluir a OS.
- Em falha irreversível, publica `execucao.falhou` para a compensação da Saga.
- Usa **MongoDB (NoSQL)** para guardar o histórico de etapas e eventos operacionais.

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
NA_FILA → EM_DIAGNOSTICO → EM_EXECUCAO → FINALIZADA
   └──────────┴──────────────┴──────────→ FALHA   (compensação)
```

---

## Saga Pattern

A Saga do ecossistema é **orquestrada pelo serviço de Ordem de Serviço**. Este serviço atua
de forma **coreografada**: reage a eventos recebidos e publica eventos de resultado, sem
orquestrar outros serviços. A escolha (orquestração no OS Service) é justificada pela
necessidade de um ponto único que acompanhe todo o ciclo da OS (criação → orçamento →
pagamento → execução) e dispare compensações.

### Tópicos Kafka do ecossistema

| Tópico | Produtor | Consumidor(es) | Papel |
|--------|----------|----------------|-------|
| `os.criada` | Ordem de Serviço | Orçamento/Pagamento | Happy path |
| `orcamento.gerado` | Orçamento/Pagamento | Ordem de Serviço | Tracking |
| `orcamento.aprovado` | Orçamento/Pagamento | Ordem de Serviço | Happy path |
| `orcamento.reprovado` | Orçamento/Pagamento | Ordem de Serviço | Compensação |
| `pagamento.confirmado` | Orçamento/Pagamento | **Execução**, Ordem de Serviço | Libera execução |
| `pagamento.recusado` | Orçamento/Pagamento | Ordem de Serviço | Compensação |
| `execucao.iniciada` | **Execução** | Ordem de Serviço | Tracking |
| `execucao.finalizada` | **Execução** | Ordem de Serviço | Conclui a OS |
| `execucao.falhou` | **Execução** | Ordem de Serviço | Compensação |

**Convenções:** chave de partição = `osId`; para cada tópico consumido há `<tópico>.retry`
e `<tópico>.DLT` (dead-letter) para tolerância a falhas.

**Este serviço** consome `pagamento.confirmado` e publica `execucao.iniciada`,
`execucao.finalizada` e `execucao.falhou`.

---

## Stack

- Java 21, Spring Boot 3.3.x
- Spring Data MongoDB (NoSQL)
- Spring for Apache Kafka
- MapStruct, Lombok
- springdoc-openapi (Swagger)
- Actuator + Micrometer/Prometheus + logs JSON (Datadog) — observabilidade da Fase 3
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

## Testes

```bash
make test     # unitários + relatório de cobertura
make verify   # testes + gate de cobertura (mínimo 80%)
```

---

## CI/CD e Kubernetes

Pipelines independentes em GitHub Actions (`.github/workflows/`):

- **`ci.yml`** — em PRs e pushes: build → testes (unitários + integração/BDD com Testcontainers) → qualidade (SonarQube). É o check obrigatório para merge na `main`.
- **`cd.yml`** — em push na `main`: build e push da imagem para o **GHCR** → deploy no Kubernetes (`kubectl apply -k k8s/`), criando o Secret a partir dos GitHub Secrets. O deploy é pulado se `KUBE_CONFIG` não estiver configurado.

Manifestos Kubernetes e instruções de deploy em [`k8s/`](k8s/README.md).

### Proteção da branch `main`

Configurar em **Settings → Branches → Branch protection rules** para `main`:
- Require a pull request before merging (≥1 aprovação);
- Require status checks to pass → selecionar **`🔨 Build, Testes e Qualidade`** (job do `ci.yml`);
- Require branches to be up to date before merging.

### Secrets usados nos workflows

| Secret | Uso |
|--------|-----|
| `SONAR_TOKEN`, `SONAR_HOST_URL` | Análise SonarQube no CI (opcional) |
| `KUBE_CONFIG` | kubeconfig do cluster alvo (deploy) |
| `MONGO_INITDB_ROOT_USERNAME`, `MONGO_INITDB_ROOT_PASSWORD`, `MONGODB_URI` | Credenciais do MongoDB (deploy) |

---

## Status do desenvolvimento

- [x] Bootstrap do projeto (estrutura, build, config, observabilidade)
- [x] Domínio (Execucao, EventoOperacional, transições)
- [x] Casos de uso e ports
- [x] Persistência MongoDB
- [x] Consumer `pagamento.confirmado` + producers de eventos
- [x] Endpoints REST (fila, avanço de etapas, consulta)
- [x] Testes unitários + BDD + cobertura ≥80% (atual: 96,3%)
- [x] Dockerfile + manifestos Kubernetes
- [x] Pipeline CI/CD + Sonar
