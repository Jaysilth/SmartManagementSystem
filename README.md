# SMMS — Smart Maintenance Management System

A multi-tenant maintenance ticketing system with role-based access control (Admin, Manager, Technician, Requester), built for facilities operations across schools, hotels, and companies.

## Stack

- **Backend:** Spring Boot (Java 24), PostgreSQL, Flyway migrations, Spring Security, JJWT
- **Frontend:** React + Vite + TypeScript, Tailwind CSS, React Router, Axios, Recharts, lucide-react

## Prerequisites

- Java 24 (JDK)
- Node.js + npm
- Docker (for local Postgres)
- Maven (or use the included `mvnw` wrapper)

## Setup

### 1. Database

A `docker-compose.yml` is provided at the repo root, already configured with a persistent named volume — your data survives container deletion, restarts, and even a Docker reinstall (see Known Limitations below for why this matters).

Start it:

```powershell
docker-compose up -d
```

To stop it later without losing data:

```powershell
docker-compose down
```

**Never run `docker-compose down -v`** unless you deliberately want to destroy all data — the `-v` flag also removes the named volume.

### 2. Backend secrets

Copy `application-local.properties.example` to `SMMS/src/main/resources/application-local.properties`, then replace the placeholder JWT secret with a real random value. This file is gitignored — never commit it.

### 3. Run the backend

```powershell
cd SMMS
./mvnw spring-boot:run
```

Flyway will run all migrations automatically on startup (currently through V7). The API runs on `http://localhost:8080`, with Swagger UI available at `/swagger-ui.html` and OpenAPI docs at `/v3/api-docs`.

### 4. Run the frontend

```powershell
cd frontend
npm install
npm run dev
```

Runs on `http://localhost:5173`. CORS is configured backend-side to allow this origin — if you change the frontend's port, update `SecurityConfig.java`'s `corsConfigurationSource()` bean to match.

### 5. Create your first organization

Visit `http://localhost:5173/register` and sign up — this creates a new organization with you as its first Admin. All other users (Manager, Technician, Requester) must be created by an Admin or Manager via the "Create User" page — public registration only creates the first Admin per organization, by design (see Known Decisions below).

## Known Decisions (don't relitigate without a reason)

- **Closed user-provisioning model:** only Admins/Managers create accounts (`POST /auth/users`). Public self-registration (`POST /auth/register`) only ever creates an ADMIN in a brand-new organization — it cannot be used to join an existing org or self-assign a different role.
- **JWT stored in browser localStorage** (not an httpOnly cookie) — acceptable for the current dev/demo stage; reconsider if this handles sensitive production data at scale.
- **Loose status/role/category validation:** these are free-text strings in the database, not enum-constrained. Deliberate tradeoff for iteration speed over strict validation.
- **Ticket `organizationId`, `createdBy`, `departmentId`, and `locationId` are always derived from the authenticated user's JWT, never from client-supplied request bodies.** This is a hard security invariant — do not regress it when touching ticket creation/assignment code.

## Known Limitations

- Frontend API base URL is hardcoded in `frontend/src/lib/api.ts`, not read from an environment variable — deploying anywhere other than `localhost:8080` currently requires editing source code.
- No automated test suite yet.
- `docker-compose.yml` currently covers Postgres only — the Spring Boot backend and the frontend are still run manually as described above. Full-stack containerization is deliberately deferred until actual deployment is on the table; it would slow down the active dev/debug loop today for no present benefit.
- File attachments, real-time (WebSocket) updates, and audit logging are not implemented.
- **Real incident, worth remembering:** an earlier version of this project ran Postgres in Docker *without* a named volume (before `docker-compose.yml` existed). A Docker reinstall wiped the container and all its data permanently — every test org, user, ticket, note, and comment gone, though the schema itself was trivially rebuilt via Flyway. The current `docker-compose.yml` includes the volume from the start; don't remove it from the compose file, and never run `docker-compose down -v`.

## Full project status

See `SMMS-project-tracker.md` for the complete, itemized list of what's built, what's tested, and what's still outstanding — kept up to date after every feature.