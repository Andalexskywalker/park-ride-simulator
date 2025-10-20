# Simulador de Rede Park & Ride (P+R) — Portugal

**Projeto de portfólio** (sem método de avaliação) baseado em microserviços com Spring Boot e Spring Cloud.
Tema: otimizar a utilização de parques **Park & Ride** para reduzir tráfego e emissões nos centros urbanos.

## Stack
- **Java 21**, **Spring Boot 3.5.6**
- **Spring Cloud 2025.0.0** (Eureka, Gateway, OpenFeign)
- Actuator (health/metrics), Springdoc OpenAPI (Swagger UI)
- Docker & Docker Compose

> Nota de versões: este esqueleto usa Spring Boot **3.5.6** e Spring Cloud **2025.0.0** (compatíveis).
> Se precisares de atualizar, altera as propriedades no `pom.xml` raiz.

## Arquitetura (módulos)
- `eureka-server`: Service Registry
- `api-gateway`: Gateway para roteamento centralizado
- `service-parques`: CRUD de Parques/Zonas/Tarifários (mínimo: endpoint `/api/info`)
- `service-utilizadores`: Utilizadores e Viaturas (mínimo: `/api/info`)
- `service-sessoes`: Sessões de estacionamento (mínimo: `/api/info`)
- `service-tarifas-faturacao`: Cálculo de preço e faturas (mínimo: `/api/info`)
- `service-reco-analytics`: Recomendações e KPIs (mínimo: `/api/info`)

Cada serviço arranca e regista-se no **Eureka**. O **Gateway** expõe rotas básicas.

## Como correr
1) **Build** de todos os módulos:
```bash
mvn -q -DskipTests package
```
2) **Subir com Docker Compose** (construção + run):
```bash
docker compose up --build
```
3) Endpoints úteis:
- Eureka: http://localhost:8761
- Gateway (health): http://localhost:8080/actuator/health
- Exemplos de serviços via Gateway:
  - http://localhost:8080/parques/api/info
  - http://localhost:8080/utilizadores/api/info
  - http://localhost:8080/sessoes/api/info

> Dica: se fores correr sem Docker, exporta `EUREKA_URI=http://localhost:8761/eureka/` no ambiente.

## Próximos passos (para portfólio)
- Implementar entidades (Parque, Zona, Tarifa, Sessao, Fatura, etc.)
- Persistência (PostgreSQL) e seeds
- Autenticação JWT + roles (NORMAL, OPERADOR, ADMIN)
- Endpoints de simulação (jobs/cron) para gerar entradas/saídas
- Swagger/OpenAPI por serviço com exemplos de requests
- Dashboards (front-end) e relatórios PDF

## Licença
MIT (ou a tua preferência)


## Testar CRUD de Parques (via Gateway)
- Listar: `GET http://localhost:8080/parques/api/parques`
- Filtrar por cidade: `GET http://localhost:8080/parques/api/parques?cidade=Sintra`
- Obter por ID: `GET http://localhost:8080/parques/api/parques/1`
- Criar:
```bash
curl -X POST http://localhost:8080/parques/api/parques   -H "Content-Type: application/json"   -d '{"nome":"P+R Loures","cidade":"Loures","capacidadeTotal":300,"precoHora":1.10}'
```
- Atualizar estado:
```bash
curl -X PATCH "http://localhost:8080/parques/api/parques/1/estado?estado=FECHADO"
```
- Apagar: `DELETE http://localhost:8080/parques/api/parques/3`
- Swagger (local do serviço): `http://localhost:8081/swagger-ui/index.html`
