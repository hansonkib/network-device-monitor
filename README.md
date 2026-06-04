# Network Device Monitoring Service

A full-stack web application for managing and monitoring network infrastructure assets. Built as a BCS Group engineering assignment.

---

## Architecture

```
                       │ HTTP / REST
┌──────────────────────▼──────────────────────────────┐
│                      Backend                         │
│   Spring Boot 3.2 / Java 17                          │
│   Spring Data JPA + Flyway migrations                │
└──────────────────────┬──────────────────────────────┘
                       │ JDBC
┌──────────────────────▼──────────────────────────────┐
│                    PostgreSQL 16                      │
└─────────────────────────────────────────────────────┘
```

---

## Features

- **Register devices** — CPE, Router, Switch, Access Point, Firewall, ONT, Other
- **Submit status reports** — ONLINE / OFFLINE / DEGRADED with optional diagnostic message
- **Device list view** — current status, last report timestamp, stale indicator (>15 min without report)
- **Device detail view** — device metadata + 20 most recent status reports in chronological order
- **Auto-refresh** — device list refreshes every 30 s, detail view every 15 s
- **Stale detection** — any device that has not reported within 15 minutes is flagged

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 17+ |
| Maven | 3.8+ |
| Docker & Docker Compose | 24+ (for containerised setup) |
| PostgreSQL | 16+ (for local dev without Docker) |

---

## Quick Start — Docker (Recommended)

```bash
# Clone / enter the project
git clone <your-repo-url>
cd network-monitor

# Build and start everything
docker compose up --build

# Backend  → http://localhost:8080
```

To stop:

```bash
docker compose down
# To also remove the database volume:
docker compose down -v
```

---

## Local Development

### 1. Start PostgreSQL

```bash
docker run -d \
  --name nm-postgres \
  -e POSTGRES_DB=network_monitor \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine
```

### 2. Backend

```bash
./mvnw spring-boot:run
# API available at http://localhost:8080
```

Override DB connection via environment variables if needed:

```bash
DB_URL=jdbc:postgresql://localhost:5432/network_monitor \
DB_USERNAME=postgres \
DB_PASSWORD=postgres \
./mvnw spring-boot:run
```

---

## Database Migrations

Flyway manages the schema. Migrations live in:

```
src/main/resources/db/migration/
  V1__create_devices_and_status_reports.sql
```

Flyway runs automatically on startup. To add a new migration, create `V2__<description>.sql`.

---

## Running Tests

### Backend

```bash
./mvnw test
```

Tests use H2 in-memory database (no PostgreSQL needed). Covers:
- Unit tests — `DeviceServiceTest` (stale logic, CRUD, error cases)
- Integration tests — `DeviceControllerIntegrationTest` (full lifecycle via MockMvc)


```

Covers `StatusBadge` component rendering across all states.

---

## REST API Reference

### Register a Device

```
POST /api/devices
Content-Type: application/json

{
  "name": "Router-Core-01",
  "deviceType": "ROUTER",
  "host": "10.0.0.1",
  "location": "Data Center A"
}
```

Device types: `CPE`, `ROUTER`, `SWITCH`, `ACCESS_POINT`, `FIREWALL`, `ONT`, `OTHER`

### Submit a Status Report

```
POST /api/devices/{id}/status
Content-Type: application/json

{
  "status": "ONLINE",
  "message": "All interfaces operational"
}
```

Statuses: `ONLINE`, `OFFLINE`, `DEGRADED`

### List All Devices

```
GET /api/devices
```

Response includes `currentStatus`, `lastReportedAt`, and `stale` (true if no report in last 15 minutes).

### Get Device Detail

```
GET /api/devices/{id}
```

Response includes the 20 most recent status reports ordered by timestamp descending.

---

## Design Decisions & Assumptions

1. **Stale threshold is 15 minutes** — computed at query time on the backend, not stored. This means stale state is always accurate without a scheduled job.

2. **Status is stored as a report, not a field on Device** — this preserves history and keeps the domain model clean. Current status is derived from the latest report.

3. **A device with no reports is considered stale** — a newly registered device that hasn't checked in yet should alert operators.

4. **Flyway for migrations** — ensures reproducible schema across all environments, including CI/CD.

5. **UUID primary keys** — avoids sequential ID enumeration and is suitable for distributed systems.

6. **React Query** — handles caching, background refetching, and stale-while-revalidate patterns with minimal boilerplate.

7. **Problem Details (RFC 9457)** — Spring's `ProblemDetail` used for consistent error responses.

---

## Project Structure

```
network-device-monitor/
│   ├── src/
│   │   ├── main/java/com/bcsgroup/networkmonitor/
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── service/         # Business logic
│   │   │   ├── repository/      # Spring Data JPA repos
│   │   │   ├── model/           # JPA entities + enums
│   │   │   ├── dto/             # Request / response records
│   │   │   ├── exception/       # Custom exceptions + global handler
│   │   │   └── config/          # CORS configuration
│   │   └── resources/
│   │       └── db/migration/    # Flyway SQL migrations
│   └── pom.xml
├── docker-compose.yml
└── README.md
```
