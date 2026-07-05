# Property Management SaaS Platform

A multi-tenant, cloud-native Property Management ERP built for the
Ethiopian real estate market. Designed to replace fragmented spreadsheets
and manual processes with a structured, automated operational backbone.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (Eclipse Temurin) |
| Framework | Spring Boot 3.5.15 |
| Security | Spring Security 6 · JWT (JJWT 0.12.6) |
| Persistence | Spring Data JPA · Hibernate 6 |
| Database | PostgreSQL 16 |
| Migrations | Liquibase 4.31.1 |
| Build | Apache Maven |
| Documentation | springdoc-openapi 2.8.5 |

## Architecture

Multi-tenant SaaS with row-level tenant isolation enforced at three
independent layers: JWT filter (application), Hibernate @Filter (ORM),
and PostgreSQL Row Level Security (database engine).

Dual-schema database topology:
- `public` — all tenant-scoped business data
- `spring_batch` — isolated batch processing metadata (Phase 2)

## Local Development Setup

### Prerequisites
- Java 21 (Eclipse Temurin)
- Docker Desktop
- Maven 3.9+

### Start the database
```bash
docker compose -f local-infra/docker-compose.yml up -d
```

### Run the application
```bash
mvn spring-boot:run
```

The application starts on `http://localhost:8080`.
OpenAPI docs available at `http://localhost:8080/swagger-ui/index.html`.

## API Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | /api/v1/organizations | Public | Register a new organization |
| POST | /api/v1/auth/login | Public | Login and obtain JWT tokens |
| POST | /api/v1/auth/refresh | Public | Refresh access token |
| POST | /api/v1/staff-members | JWT · ADMINISTRATOR | Invite staff member |
| GET | /api/v1/staff-members | JWT · Any role | List staff members |
| PATCH | /api/v1/staff-members/{id}/deactivate | JWT · ADMINISTRATOR | Deactivate account |
| PATCH | /api/v1/staff-members/{id}/reactivate | JWT · ADMINISTRATOR | Reactivate account |
| PATCH | /api/v1/staff-members/{id}/role | JWT · ADMINISTRATOR | Change role |

## Current Milestone

`v0.1.0` — Foundation complete.
Multi-tenant auth backbone, organization management, and staff member
lifecycle verified end-to-end including adversarial tenant isolation tests.

## Roadmap

| Version | Milestone |
|---|---|
| `v0.1.0` | ✅ Foundation — Auth backbone, Org + Staff management |
| `v0.2.0` | ✅ Core Inventory — Property, Structure, Unit hierarchy |
| `v0.3.0` | ✅ Tenant & Lease Management |
| `v0.4.0` | ✅ Billing Engine — Spring Batch, idempotent invoicing |
| `v1.0.0` | 🔲 MVP Release |

## License

Private — All rights reserved.
© 2026 Befiker Gezahegn Hailemichael
