# Kubernetes — Execução da Oficina

Manifestos para o deploy do microsserviço no Kubernetes.

## Componentes

| Arquivo | Recurso |
|---------|---------|
| `namespace.yaml` | Namespace `auto-center-execucao` |
| `configmap.yaml` | Config não-sensível (perfil, porta, Kafka, logging) |
| `secret.example.yaml` | **Template** do Secret (credenciais do Mongo e `MONGODB_URI`) — não versione o real |
| `mongodb-statefulset.yaml` + `mongodb-service.yaml` | MongoDB (NoSQL) com PVC e service headless |
| `app-deployment.yaml` | Deployment da aplicação (init container aguarda o Mongo, anotações Datadog, probes) |
| `app-service.yaml` | Service NodePort (`30098` → `8098`) |
| `hpa.yaml` | HorizontalPodAutoscaler (CPU 70% / memória 80%, 2–10 réplicas) |
| `kustomization.yaml` | Orquestra a ordem de aplicação |

> O **Kafka** é a mensageria compartilhada do ecossistema; aponte `KAFKA_BOOTSTRAP_SERVERS`
> no `configmap.yaml` para o cluster Kafka existente (não é provisionado aqui).

## Pré-requisitos

1. Imagem publicada no registry e referenciada em `app-deployment.yaml`.
2. Criar o Secret (não versionado) antes do deploy:

```bash
kubectl create namespace auto-center-execucao
kubectl create secret generic execucao-secrets -n auto-center-execucao \
  --from-literal=MONGO_INITDB_ROOT_USERNAME=execucao \
  --from-literal=MONGO_INITDB_ROOT_PASSWORD='<senha>' \
  --from-literal=MONGODB_URI="mongodb://execucao:<senha>@mongodb-service:27017/execucao?authSource=admin"
```

## Deploy

```bash
kubectl apply -k k8s/
```

## Verificação

```bash
kubectl get pods -n auto-center-execucao
kubectl get svc  -n auto-center-execucao
# Health:  http://localhost:30098/actuator/health
# Swagger: http://localhost:30098/swagger-ui.html
```
