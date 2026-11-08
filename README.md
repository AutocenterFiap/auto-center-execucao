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

- Pipeline independente (build → testes → qualidade/Sonar → imagem → deploy K8s).
- Branch `main` protegida (PR obrigatório + checagens).
- Manifestos Kubernetes e deploy automatizado em `k8s/` (a adicionar).

---

## Status do desenvolvimento

- [x] Bootstrap do projeto (estrutura, build, config, observabilidade)
- [ ] Domínio (Execucao, EventoOperacional, transições)
- [ ] Casos de uso e ports
- [ ] Persistência MongoDB
- [ ] Consumer `pagamento.confirmado` + producers de eventos
- [ ] Endpoints REST (fila, avanço de etapas, consulta)
- [ ] Testes unitários + BDD + cobertura ≥80%
- [ ] Dockerfile + manifestos Kubernetes
- [ ] Pipeline CI/CD + Sonar
