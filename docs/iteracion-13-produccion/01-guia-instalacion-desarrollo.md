# 01 — Guía de instalación y desarrollo

## Requisitos

| Herramienta | Versión | Uso |
|---|---|---|
| JDK | **21 LTS** | Backend (Spring Boot 3.3) |
| Maven | 3.9+ | Build backend |
| Node.js | 22+ | Portal Angular |
| Flutter | última estable | App móvil |
| Docker + Compose | reciente | Infra local (PostGIS, Redis, MinIO…) |

## Arranque rápido (todo el stack)

```bash
# 1) Infra + backend + web
cp infra/.env.example infra/.env
docker compose -f infra/docker-compose.yml up -d --build

# Servicios:
#   Portal (NGINX)      http://localhost:8081
#   API                 http://localhost:8080/api/v1
#   Swagger UI          http://localhost:8080/swagger-ui.html
#   MinIO consola       http://localhost:9001
#   MailHog             http://localhost:8025
#   Prometheus          http://localhost:9090
#   Grafana             http://localhost:3000
```

## Desarrollo por proyecto

### Backend
```bash
cd backend
docker compose -f ../infra/docker-compose.yml up -d postgres redis minio mailhog
mvn -pl bootstrap spring-boot:run -Dspring-boot.run.profiles=dev   # requiere JDK 21
mvn verify                                                          # unit + IT (Testcontainers, requiere Docker)
```
El perfil `dev` siembra la empresa **DEMO** y usuarios de prueba:
`admin@demo.com` / `Admin123!` (COMPANY_ADMIN), `empleado@demo.com` / `Admin123!` (EMPLOYEE).

Probar login:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@demo.com","password":"Admin123!","companyCode":"DEMO"}'
```

### Portal web (Angular)
```bash
cd web
npm install
npm start           # http://localhost:4200
npm run build       # producción (PWA)
npm test            # Karma/Jasmine
```

### App móvil (Flutter)
```bash
cd mobile
flutter pub get
flutter gen-l10n
dart run build_runner build --delete-conflicting-outputs   # Drift/Freezed/Riverpod
flutter run
flutter test
```

## Estructura del repositorio
Ver [README](../../README.md#estructura-del-repositorio-destino). Convenciones y arquitectura en [`docs/iteracion-02-arquitectura`](../iteracion-02-arquitectura/).
