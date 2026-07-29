# SMMS Project Tracker

Last updated: 2026-07-27 (README + env template created; recovered from a real Postgres data-loss incident, volume fix applied)

## Stack
- Backend: Spring Boot (Java), PostgreSQL, Flyway, Spring Security, JJWT
- Frontend: React + Vite + TypeScript, Tailwind CSS, React Router, Axios

---

## ✅ DONE

### Backend
- [x] Postgres running in Docker (host port 5433 → container 5432)
- [x] Flyway migrations: V1 (org/ticket tables + seed data), V2 (app_user table), V3 (ticket.created_by)
- [x] Tenant isolation proven at query level (organizationId scoping)
- [x] Registration endpoint (`POST /auth/register`) — creates org + user, transactional, Argon2 password hashing, role selectable
- [x] Login endpoint (`POST /auth/login`) — JWT issuance, generic error message (no email enumeration)
- [x] JWT auth filter — replaces old X-Org-Id header, extracts org/role/userId from token claims
- [x] Global exception handler — IllegalArgumentException → 401 (not 500)
- [x] CORS configured for `http://localhost:5173`
- [x] Ticket read (`GET /tickets`) — org-scoped
- [x] Ticket create (`POST /tickets`) — org from token, not client input
- [x] RBAC v0 — full matrix tested, all 4 roles confirmed (not assumed):
    - Create: Admin ✅, Manager ✅, Requester ✅ allowed; Technician ✅ blocked (403)
    - Read: Admin ✅, Manager ✅, Technician ✅ see all org tickets; Requester ✅ sees only own (`createdBy` scoped)
- [x] Repo pushed to GitHub (https://github.com/Jaysilth/SmartManagementSystem) — verified clean: no node_modules, no target/, no JWT secret in tracked history

### Frontend
- [x] Vite + React + TypeScript scaffold
- [x] Tailwind CSS configured and confirmed working
- [x] Axios instance with automatic Bearer token attachment
- [x] Login page
- [x] Registration page
- [x] Protected routes (redirect to /login if no token)
- [x] Ticket list page (fetches + displays)
- [x] Ticket creation form (create + auto-refresh list)
- [x] Logout (clears token, redirects)

---

## ⬜ TO DO (in rough priority order)

### Security / config hygiene (do before anything public)
- [x] `.gitignore` confirmed excluding secrets, node_modules, target/ — verified on live GitHub repo
- [x] JWT secret moved to gitignored `application-local.properties`, loaded via `spring.config.import=optional:...`
- [ ] CORS allowed-origins list needs the real deployed frontend URL added before deployment (currently hardcoded to localhost:5173)
- [ ] Note: JWT secret and old test credentials exist in earlier git *history* (pre-lockdown commits) — low risk since only test passwords were involved, but if a real secret is ever committed, deleting the file isn't enough; the commit itself must be rewritten or the secret rotated.

### User management (biggest functional gap) — CLOSED
- [x] Admin/Manager-only `POST /auth/users` endpoint — creates a user with a specified role in the *inviter's* organization only (org derived from token, not client input)
- [x] Public self-registration locked down — `POST /auth/register` now always creates ADMIN regardless of any `role` field sent; role field removed from `RegisterRequest` entirely. Confirmed via direct test (sent `role:"TECHNICIAN"`, got back `role:"ADMIN"`).
- [x] Frontend: user management screen for Admins/Managers — `CreateUser.tsx` page, role dropdown, client-side role gate (backend enforces the real restriction) + link from Tickets page
    - Note: create-only. No list of existing org users, no edit role, no delete — those remain unbuilt (spec says Admin can edit/delete, Manager cannot).
- [x] Frontend: `getCurrentUser()` helper (`src/lib/auth.ts`, using `jwt-decode`) — decodes JWT claims client-side for role-based UI gating. Not a security boundary — backend `@PreAuthorize`-equivalent checks are the real enforcement.
- [x] Frontend: `ManageLocations.tsx` — Admin/Manager-only screen to create departments and locations (with parent-location dropdown for nesting), tested working end to end including nested-location creation
- [ ] `POST /auth/users` lives under an odd URL (`/auth/users` instead of `/users`) — known naming compromise, not fixed, would need a dedicated UserController

### RBAC — remaining pieces from the full spec (not yet built)
- [x] Department + Location entities (sibling model, not nested — Location self-references via `parent_location_id` for physical hierarchy; Department is a separate org-scoped concern for routing/responsibility, not physical containment). Migration V5.
- [x] Department CRUD backend: `GET/POST /departments` (read open to all roles, create Admin+Manager), `DELETE /departments/{id}` (Admin only)
    - Tested: Manager create → 201, Manager delete → 403, Admin delete → 204
- [x] Location CRUD backend: `GET/POST /locations` (read open to all roles, create Admin+Manager), `DELETE /locations/{id}` (Admin only)
    - Self-referencing `parentLocationId` for physical nesting (Campus → Building → Room), cross-org parent nesting blocked (validated: attempting to nest under another org's location correctly returns 404)
    - Tested: top-level location create, nested child create, cross-org parent-nesting attack blocked
    - Delete role-gating (Manager 403 / Admin 204) not re-tested — identical code path already proven twice via Department
- [x] Ticket assignment (`PATCH /tickets/{id}/assign`) — Manager/Admin only, sets `assignedTechnicianId` + status to ASSIGNED
    - Migration V4: added `status` (default OPEN) and `assigned_technician_id` columns
    - Org-scoped lookup (`findByIdAndOrganizationId`) proven to block cross-tenant assignment — tested with a fresh valid token from a different org, correctly denied
    - Technician correctly blocked (403) from assigning
    - Known gap: no verification that `technicianId` in the request actually belongs to the same org or holds the TECHNICIAN role — an Admin could currently assign to any user ID. Not fixed yet.
    - Known bug: cross-org / not-found ticket access returns 401 (via generic IllegalArgumentException → GlobalExceptionHandler), should be 403/404 — semantically wrong status code, though access is still correctly blocked. Not fixed yet.
- [ ] Ticket status/state machine: Open → Assigned → Accepted → In Progress → Waiting for Parts → Completed → Closed (+ Reopened, Cancelled)
- [x] Frontend: ticket status badge + technician-assignment dropdown on `Tickets.tsx` (Manager/Admin only) — tested end to end, status updates to ASSIGNED without manual refresh
- [x] Backend: `GET /auth/users?role=X` endpoint — Admin/Manager-only, org-scoped, powers the technician dropdown
- [x] Bug fixed: CORS `allowedMethods` was missing `PATCH`, blocking the assign request entirely — added, confirmed working
- [x] Frontend: Technician status-update buttons on `Tickets.tsx` — gated to tickets assigned to the logged-in technician specifically (compares `currentUser.sub` converted to number against `assignedTechnicianId`), tested working with live status transitions, no manual refresh needed
- [x] Axios response interceptor added (`api.ts`) — catches 401, clears token, redirects to `/login`. Excludes `/auth/login` and `/auth/register` so genuine login-form errors render normally instead of triggering a false "session expired" redirect.
- [x] Backend exception-type split — replaced generic `IllegalArgumentException` with three specific types: `InvalidCredentialsException` (401), `DuplicateResourceException` (409), `ResourceNotFoundException` (404). Fixes the earlier bug where cross-org/not-found ticket access incorrectly returned 401.
    - Tested: wrong password → 401 (shown correctly in UI, no false redirect), duplicate email on register → 409, cross-org ticket assign attempt → 404 (confirmed via fresh Admin token from a different org)
- [x] Technician: can only update status on tickets assigned to them
    - `PATCH /tickets/{id}/status` — assigned Technician or Manager/Admin only, org-scoped lookup
    - Status enforcement is loose (no strict state-machine validation of transitions yet) — deliberate choice, revisit once real usage patterns are known
    - Tested: assigned Technician succeeds (200), a different unrelated Technician on the same ticket correctly blocked (403)
- [x] Requester: close/cancel/reopen own tickets — extends the existing `PATCH /tickets/{id}/status` endpoint (no new endpoint needed)
    - Requester can set CLOSED or CANCELLED on their own ticket unconditionally; REOPENED only if current status is COMPLETED
    - Requester blocked (403) from arbitrary statuses (e.g. IN_PROGRESS) and from any ticket they didn't create
    - Tested all 4 branches individually: bad status → 403, CANCELLED → 200, non-owner → 403, COMPLETED→REOPENED → 200 (the one conditional branch, not just the always-true ones)
    - Note: Requester's transition rules are strictly enforced (only 3 named moves allowed); Technician/Manager/Admin remain loosely enforced (any status accepted) — deliberate asymmetry matching the spec, not an inconsistency
- [x] Frontend: Requester Close/Cancel/Reopen buttons on `Tickets.tsx` — gated to own tickets only, Reopen only shown when status is COMPLETED, Close/Cancel hidden once already CLOSED/CANCELLED. Reuses the same `handleStatusChange` function as the Technician UI. Tested working.
- [ ] Requester: can edit own ticket details (title/description) only while status is Open or Reopened — separate from status transitions, not yet built
- [x] Work notes & comments backend (create + read only, per agreed smallest-slice scope) — `work_note` and `ticket_comment` tables, both org+ticket-scoped via `TicketNoteController`
    - Work notes: Technician/Manager/Admin only, 403 for Requester (matches spec exactly)
    - Comments: open to all roles, no ownership restriction (spec says "all roles" — read as any org member, not owner-only; flagged as an interpretation, not re-confirmed against spec wording)
    - Real bug found and fixed: `createdAt` returned `null` on the create response (though correct on subsequent reads) — root cause was `insertable=false/updatable=false` relying on a DB-level `DEFAULT now()` that Hibernate never re-read into the in-memory object post-insert. First attempted fix (manual refetch via `findById` right after `save`) didn't work — same Hibernate session returns the cached in-memory object, never hits the DB. Real fix: `@CreationTimestamp` annotation, letting Hibernate generate the value itself at insert time.
    - Second bug: the `@CreationTimestamp` fix was initially applied only to `WorkNote`, missed on `TicketComment` — caught because comments were tested with a fresh create afterward, not assumed fixed by association
    - Tested: Technician creates work note (real timestamp confirmed), Requester blocked from work notes (403), Requester creates + reads comments (both timestamped correctly, correct chronological order)
- [x] Frontend: expandable "Show details" panel per ticket — comments (all roles) and work notes (hidden entirely from Requester, both UI and the underlying API call) with post + live re-fetch. Tested working across roles.
- [ ] Audit logging (Admin-only visibility)
- [x] Dashboard backend (v1 scope: open-ticket count + status breakdown only — technician workload, avg resolution time, recently completed, and Admin/Technician-specific views deferred). `GET /dashboard`, Admin/Manager only.
    - Tested: counts match actual ticket data, Technician correctly blocked (403)
- [x] Frontend: `Dashboard.tsx` — open-count card + status-breakdown bar chart (recharts), Admin/Manager only, matching the established card design. Tested working, values confirmed matching backend exactly.

### Ticket features
- [x] Ticket create now accepts optional `departmentId`/`locationId`, both org-scoped-validated before acceptance (cross-org attempt correctly returns 404 `Department not found`/`Location not found`)
- [x] Frontend: ticket-creation form now includes department/location dropdowns (open to all roles, matching backend's open GET endpoints), tested working — payload and response both confirmed round-tripping correctly
- [x] Description, category, priority fields added (`description` TEXT, `category` VARCHAR, `priority` VARCHAR defaulting to MEDIUM). Loose validation (no enum/CHECK constraint), matching the `role`/`status` precedent. Migration V6.
    - Tested: full-field creation round-trips correctly; omitting priority correctly falls back to MEDIUM (server-side default, not just DB default)
- [x] Frontend: description textarea, category/priority dropdowns added to ticket-creation form; ticket list now displays priority/category as badges and description as a line below the title. Tested working, including clean rendering of old tickets with null description/category (no stray "null" text).
- [x] Light UI component pass — extracted `Badge` component (color-coded priority tones, JetBrains Mono uppercase for data labels), added `lucide-react` icons throughout, applied a consistent bordered-card + offset-shadow treatment to Login, Register, CreateUser, and ManageLocations. Fonts: Inter (body/labels) + JetBrains Mono (data/kicker labels), self-hosted via `@fontsource`. All functionality unchanged — visual pass only, verified working on every page touched.
    - Fixed during review: Departments "Add" button was overflowing its card due to a side-by-side flex layout crowding a fixed-width card; restructured to match Locations' stacked full-width layout — both columns now visually consistent
- [ ] File/photo attachments
- [x] Search & filter (v1 scope: search by title, status, priority, category — department/technician/date-range deferred to a later increment). Refactored `GET /tickets` into a single JPQL query with optional parameters, folding the Requester owner-scoping logic into the same query instead of branching Java code.
    - Real bug caught and fixed: `LOWER(:search)` failed with `function lower(bytea) does not exist` when search was null — Postgres/JDBC couldn't infer the parameter's type from an untyped null, defaulting to bytea. Fixed via explicit `CAST(:search AS string)`.
    - Tested: unfiltered (regression-free after refactor), status filter, search filter (the case that had the bug), and — most important — Requester owner-scoping re-verified post-refactor since it's a tenant/ownership boundary, not just a convenience feature
- [x] Frontend: filter bar (search input + status/priority/category dropdowns) added above the ticket list, re-fetches via axios `params` on any change. Tested working across all four filters individually.
    - Known follow-up: search input has no debounce — every keystroke fires a request. Fine at current scale, worth adding a ~300ms debounce if ticket volume grows.

### Real-time & infra (from original spec, not started)
- [ ] Socket.IO or WebSocket real-time updates (assignment, status change, comments)
- [ ] Email notifications (ticket created, assigned, resolved)
- [ ] Cloud file storage (S3/R2) for attachments
- [x] README.md + application-local.properties.example created — setup instructions, known decisions, known limitations
    - **Real incident during this work:** Docker container running Postgres had no persistent volume. A Docker reinstall wiped it entirely — all test data (orgs, users, tickets, notes, comments) permanently lost. Schema was trivially recovered via Flyway re-running all 7 migrations on the empty DB (including the V1 seed data, which is why a fresh registration landed on organizationId 3, not 1). README corrected to make the volume step mandatory going forward — always run `docker volume create apex-db-data` and mount it with `-v apex-db-data:/var/lib/postgresql/data`.
- [x] Docker Compose for Postgres (v1 scope: database only — backend/frontend still run manually, deliberately deferred until deployment is actually needed, since containerizing the backend now would slow the active dev/debug loop for no present benefit)
    - Real subtlety caught: Compose defaults to prefixing volume names with the project folder name (e.g. `smms_apex-db-data`), which silently creates a *second*, different volume instead of reusing an existing one of the same name. Fixed via explicit `name: apex-db-data` in the volume definition.
    - Tested for real, not just visually: tore down and recreated the container via `docker compose down` / `up`, then queried Postgres directly (`SELECT id, email FROM app_user`) and confirmed the pre-existing test Admin account survived the full container teardown/recreate cycle — genuine proof of persistence, not assumed.
- [ ] Automated tests (auth, tenant isolation, RBAC — critical paths only)

### Cleanup
- [ ] Orphaned test organizations/users from failed registration attempts during dev (orgs 1,3,4,6,7,8,10 etc — audit and delete)
- [ ] Frontend: loading states/skeletons, better error messages than generic strings
- [ ] Frontend: role-based UI gating (hide "create ticket" form for Technicians, matching backend rule)

---

## Known decisions (don't relitigate without a reason)
- Closed user-provisioning model: only Admins/Managers create accounts; no public self-service registration beyond the very first org-creation step. (Not yet implemented — registration is still fully open right now, this is the target state.)
- JWT stored in localStorage (not httpOnly cookie) — acceptable for dev stage, revisit if this goes to real production with sensitive data.
- Spring Data JPA chosen over jOOQ for simplicity at this stage.
- Ticket `organizationId` and `createdBy` always derived from the authenticated token, never from client-supplied request body — do not regress this.