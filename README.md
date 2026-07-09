# Property Management SaaS Platform

> A multi-tenant, cloud-native Property Management ERP built for the Ethiopian real estate market.
>
> Designed to replace fragmented spreadsheets, paper ledgers, and WhatsApp workflows with a structured, secure, and automated operational platform.

---

# Current Status

| Version | Status | Description |
|---------|--------|-------------|
| `v0.1.0` | ✅ Released | Foundation — JWT Authentication, Multi-Tenant Isolation, Organization & Staff Management |
| `v0.2.0` | ✅ Released | Core Inventory — Properties, Structures, Units |
| `v0.3.0` | ✅ Released | Tenant & Lease Management |
| `v0.4.0` | ✅ Released | Automated Billing Engine |
| `v0.4.1` | ✅ Released | Pilot Feedback Patch |
| `v0.5.0` | 🚧 In Progress | Automated Testing, Audit Ledger, Production Hardening |

---

# Overview

The platform enables property management companies to:

- Register their organization
- Build their property portfolio
- Manage buildings, structures, and units
- Register tenants
- Execute leases
- Automatically generate recurring invoices
- Record payments
- Maintain complete tenant isolation between competing organizations

## Core Workflow

```text
Register Organization
        │
        ▼
Create Properties
        │
        ▼
Create Structures & Units
        │
        ▼
Register Tenant
        │
        ▼
Create Lease
        │
        ▼
Activate Lease
(Unit → OCCUPIED)
        │
        ▼
Automatic Monthly Billing
        │
        ▼
Receive Payment
(Invoice → PAID)
        │
        ▼
Terminate Lease
(Unit → VACANT)
```

---

# Technology Stack

| Layer | Technology | Version |
|--------|------------|---------|
| Language | Java | 21 LTS |
| Runtime | Eclipse Temurin OpenJDK | 21 |
| Framework | Spring Boot | 3.5.15 |
| Security | Spring Security + JWT (JJWT) | 6.x / 0.12.6 |
| ORM | Hibernate | 6.x |
| Persistence | Spring Data JPA | Latest |
| Database | PostgreSQL | 16 |
| Database Migration | Liquibase | 4.31.1 |
| Batch Processing | Spring Batch | 3.x |
| Object Mapping | MapStruct | 1.6.3 |
| API Documentation | SpringDoc OpenAPI | 2.8.5 |
| Build Tool | Maven | 3.9+ |
| Containerization | Docker & Docker Compose | Latest |

---

# Architecture

## Multi-Tenant Security

Every record belongs to exactly one organization.

Tenant isolation is enforced independently at three layers.

### Layer 1 — Application

```
JWT
   ↓
JwtAuthenticationFilter
   ↓
TenantContext (ThreadLocal)
```

The authenticated tenant is extracted from the JWT and stored for the duration of the request.

---

### Layer 2 — ORM

```
TenantContext
      ↓
TenantFilterAspect
      ↓
Hibernate @Filter
      ↓
Repository Query
```

Every database query is automatically scoped to the authenticated organization.

---

### Layer 3 — Database

```
PostgreSQL
      ↓
Row Level Security (RLS)
```

Even if application logic fails, PostgreSQL still prevents cross-tenant access.

---

## Database Layout

```
PostgreSQL
│
├── public
│   ├── organizations
│   ├── staff_members
│   ├── properties
│   ├── property_structures
│   ├── units
│   ├── tenant_profiles
│   ├── leases
│   ├── invoices
│   └── payments
│
└── spring_batch
    ├── BATCH_JOB_INSTANCE
    ├── BATCH_JOB_EXECUTION
    └── BATCH_STEP_EXECUTION
```

---

# Database Constraints

| Constraint | Purpose |
|------------|---------|
| `UNIQUE(tenant_id, unit_id) WHERE status='ACTIVE'` | One active lease per unit |
| `UNIQUE(lease_id, billing_period)` | Prevent duplicate invoices |
| `NUMERIC(15,2)` | Accurate monetary values |
| `ON DELETE RESTRICT` | Prevent accidental cascading deletions |

---

# Local Development

## Prerequisites

- Java 21
- Maven 3.9+
- Docker Desktop

---

## Clone Repository

```bash
git clone https://github.com/BefikerG/property-management-saas.git

cd property-management-saas
```

---

## Start PostgreSQL

```bash
docker compose -f local-infra/docker-compose.yml up -d
```

---

## Run Application

```bash
mvn spring-boot:run
```

Application URL

```
http://localhost:8080
```

On the first startup, Liquibase automatically creates both database schemas and applies all migrations.

---

# API Documentation

Swagger UI

```
http://localhost:8080/swagger-ui/index.html
```

OpenAPI Specification

```
http://localhost:8080/v3/api-docs
```

---

# Organization Onboarding

## Register Organization

```bash
curl -X POST http://localhost:8080/api/v1/organizations \
-H "Content-Type: application/json" \
-d '{
  "name":"Your Firm",
  "adminEmail":"admin@company.com",
  "adminPassword":"SecurePass123!",
  "adminFullName":"Administrator"
}'
```

---

## Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
-H "Content-Type: application/json" \
-d '{
  "email":"admin@company.com",
  "password":"SecurePass123!"
}'
```

Use the returned JWT for all subsequent requests.

```
Authorization: Bearer <accessToken>
```

---

# API Overview

## Authentication

| Method | Endpoint | Access |
|---------|----------|--------|
| POST | `/api/v1/auth/login` | Public |
| POST | `/api/v1/auth/refresh` | Public |

---

## Organizations

| Method | Endpoint |
|---------|----------|
| POST | `/api/v1/organizations` |
| GET | `/api/v1/organizations/me` |
| PUT | `/api/v1/organizations/me` |

---

## Staff Members

| Method | Endpoint |
|---------|----------|
| POST | `/api/v1/staff-members` |
| GET | `/api/v1/staff-members` |
| GET | `/api/v1/staff-members/{id}` |
| PATCH | `/api/v1/staff-members/{id}/deactivate` |
| PATCH | `/api/v1/staff-members/{id}/reactivate` |
| PATCH | `/api/v1/staff-members/{id}/role` |

---

## Properties

| Method | Endpoint |
|---------|----------|
| POST | `/api/v1/properties` |
| GET | `/api/v1/properties` |
| GET | `/api/v1/properties/{id}` |
| PUT | `/api/v1/properties/{id}` |
| DELETE | `/api/v1/properties/{id}` |

---

## Structures

| Method | Endpoint |
|---------|----------|
| POST | `/api/v1/properties/{id}/structures` |
| GET | `/api/v1/properties/{id}/structures` |

---

## Units

| Method | Endpoint |
|---------|----------|
| POST | `/api/v1/properties/{id}/units` |
| GET | `/api/v1/properties/{id}/units` |
| GET | `/api/v1/properties/{id}/units/{unitId}` |
| PATCH | `/api/v1/properties/{id}/units/{unitId}/status` |

---

## Tenant Profiles

| Method | Endpoint |
|---------|----------|
| POST | `/api/v1/tenant-profiles` |
| GET | `/api/v1/tenant-profiles` |
| GET | `/api/v1/tenant-profiles/{id}` |
| PUT | `/api/v1/tenant-profiles/{id}` |
| DELETE | `/api/v1/tenant-profiles/{id}` |

---

## Leases

| Method | Endpoint |
|---------|----------|
| POST | `/api/v1/leases` |
| GET | `/api/v1/leases` |
| GET | `/api/v1/leases/{id}` |
| POST | `/api/v1/leases/{id}/activate` |
| POST | `/api/v1/leases/{id}/terminate` |
| POST | `/api/v1/leases/{id}/expire` |

---

## Invoices

| Method | Endpoint |
|---------|----------|
| GET | `/api/v1/invoices` |
| GET | `/api/v1/invoices/{id}` |
| POST | `/api/v1/invoices/{id}/payments` |
| GET | `/api/v1/invoices/{id}/payments` |

---

## Billing

| Method | Endpoint |
|---------|----------|
| POST | `/api/v1/billing/trigger` |

---

# Roles & Permissions

| Permission | Administrator | Property Manager | Viewer |
|------------|--------------|------------------|--------|
| Manage Staff | ✅ | ❌ | ❌ |
| Organization Settings | ✅ | ❌ | ❌ |
| Trigger Billing | ✅ | ❌ | ❌ |
| Manage Properties | ✅ | ✅ | ❌ |
| Manage Leases | ✅ | ✅ | ❌ |
| Register Tenants | ✅ | ✅ | ❌ |
| Record Payments | ✅ | ✅ | ❌ |
| View Organization Data | ✅ | ✅ | ✅ |

---

# Unit State Machine

```text
             Lease Activated
VACANT --------------------------► OCCUPIED
   ▲                                 │
   │                                 │
   │ Lease Terminated / Expired      │
   │                                 │
   └───────────────◄─────────────────┘

VACANT ◄────────► MAINTENANCE

(Manual Override Only)
```

Only lease activation may transition a unit into **OCCUPIED**.

---

# Invoice State Machine

```text
UNPAID
   │
   ├────────► PARTIALLY_PAID
   │
   └────────► PAID
```

Invoice status is calculated automatically using `BigDecimal`.

---

# Database Migrations

| Version | Description |
|---------|-------------|
| V001 | Organizations |
| V002 | Staff Members |
| V003 | Properties |
| V004 | Property Structures |
| V005 | Units |
| V006 | Tenant Profiles |
| V007 | Leases |
| V008 | Invoices |
| V009 | Payments |
| V010 | Spring Batch Schema |

Liquibase automatically applies all migrations during startup.

---

# Roadmap

| Version | Milestone | Status |
|---------|-----------|--------|
| v0.1.0 | Authentication & Organization Management | ✅ |
| v0.2.0 | Property Inventory | ✅ |
| v0.3.0 | Tenant & Lease Management | ✅ |
| v0.4.0 | Billing Engine | ✅ |
| v0.4.1 | Pilot Feedback | ✅ |
| v0.5.0 | Automated Testing & Audit Ledger | 🚧 |
| v1.0.0 | MVP General Availability | 📅 Planned |

---

# Contributing

This is currently a private commercial project.

Development follows a professional Git workflow:

- Feature branches
- Pull Requests into `develop`
- Semantic Versioning
- Conventional Commits
- Milestone tagging
- Production releases from `main`

---

# License

**Private — All Rights Reserved**

© 2026 Befiker Gezahegn Hailemichael
