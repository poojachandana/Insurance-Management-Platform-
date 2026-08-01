# 🛡️ Insurance Management Platform

**🔗 Live Demo:** https://insurance-management-platform-psi.vercel.app/


> Note: the backend is hosted on Render's free tier, which spins down after 15 minutes of inactivity. The first request after idling may take 30–60 seconds to respond while it wakes up.

---

A full-stack Insurance Management Platform that lets insurance companies, agents, and
customers manage policies, claims, premium payments, and documents from a centralized
system — covering the complete lifecycle from customer registration to claim settlement.

Built with role-based access for **Admin**, **Agent**, and **Customer** users, each with
a distinct dashboard and permission set.

- **Backend:** Java 21, Spring Boot 3, Spring Security + JWT, Spring Data JPA (Hibernate), H2 (local) / PostgreSQL (production), OpenPDF, SpringDoc OpenAPI (Swagger)
- **Frontend:** React.js (Vite), Tailwind CSS, Axios, React Router, Chart.js
- **Deployment:** Render (backend, via Docker) + Neon Postgres (database) + Vercel (frontend)

---

## Demo Credentials

~~~
Admin login:
  email:    admin@insurance.com
  password: Admin@123
~~~

Agent and Customer accounts are created through the app itself (Admin creates Agents;
anyone can self-register as a Customer via the Register page).

---

## 1. Project Structure

~~~
insurance-management-platform/
├── backend/     Spring Boot API (Maven project — open this folder in IntelliJ)
│   ├── src/main/java/com/insurance/platform/
│   │   ├── entity/       Database tables (JPA entities)
│   │   ├── dto/          Request/response objects
│   │   ├── repository/   Data access interfaces
│   │   ├── service/      Business logic interfaces
│   │   ├── service/impl/ Business logic implementations
│   │   ├── controller/   REST API endpoints
│   │   ├── security/     JWT authentication
│   │   ├── config/       Security rules, CORS, data seeding
│   │   └── exception/    Global error handling
│   ├── Dockerfile        Used for Render deployment
│   └── src/main/resources/
│       ├── application.properties       Local dev config (H2)
│       └── application-prod.properties  Production config (Postgres via env vars)
└── frontend/    React + Vite client
    └── src/
        ├── pages/       One component per screen
        ├── components/  Shared UI (Pagination, StatusBadge, PasswordInput, etc.)
        ├── services/    API call wrappers (axios)
        ├── context/     Auth + Theme (dark mode) global state
        └── layouts/     Dashboard shell (sidebar, header, notifications)
~~~

---

## 2. Features

### Customer Management
Register, view, edit, and search customers. Customers can self-register via a public
sign-up page or be registered directly by an Agent. Self-registered customers start
**Unassigned** until an Admin routes them to an Agent.

### Policy Management
Create, renew, and cancel policies against structured **Insurance Categories** (Health,
Life, Motor, Home, Travel, Marine — Admin-managed). Policies auto-expire on their end
date via a daily scheduled job, and expiry reminders fire automatically ahead of time
(configurable window in Settings).

### Claim Management
Customers submit claims and attach supporting documents (either uploading fresh or
selecting from previously uploaded files). Agents verify documents inline before
approving or rejecting. Admins can assign any claim to a specific Agent.

### Premium Tracking
Schedule premium payments against a policy, mark them paid, and track status (Due /
Paid / Overdue). A daily job automatically flags anything past its due date + grace
period as Overdue and notifies the customer.

### Document Management
Upload, view, and download Identity, Policy, and Claim documents. Admin/Agent get a
consolidated document review screen across all customers.

### Reports Dashboard
Live charts: policy status breakdown, claim statistics, customer growth, monthly
premium collection — plus a downloadable PDF business summary.

### Employee Management (Admin)
Create and manage Agent/Admin accounts, with safeguards against removing yourself or
the last remaining Admin.

### Audit Logs (Admin)
A searchable, paginated trail of every significant action (create/update/delete/
approve/assign/pay) across the system, including automated scheduled jobs.

### System Settings (Admin)
Company details plus the automation rules (default policy term, expiry reminder
window, premium grace period) that drive the scheduled background jobs.

### Notifications
In-app alerts (bell icon) for policy expiry reminders and overdue premiums.

### Role-Scoped Visibility
Admins see everything. Agents only see customers they registered (or were explicitly
assigned) — and everything under those customers (policies, claims, premiums,
documents) is scoped the same way.

### Other
- Dark mode toggle
- Search, filter, and pagination on all major list views
- Self-service profile editing and password change for every role
- Three-tab login (Customer / Agent / Admin) with mismatch detection

---

## 3. Running Locally (IntelliJ IDEA)

### Backend
1. **File → Open** → select the `backend` folder.
2. Let IntelliJ import the Maven project (needs internet for dependencies).
3. Set Project SDK to **Java 21** (File → Project Structure → SDK).
4. Confirm annotation processing is enabled (Settings → Build, Execution, Deployment
   → Compiler → Annotation Processors → check "Enable annotation processing") — needed
   for Lombok.
5. Run `InsuranceManagementPlatformApplication.java`, or from a terminal:
   ~~~bash
   cd backend
   mvn spring-boot:run
   ~~~
6. API starts on **http://localhost:8080**.

Uses an in-memory **H2 database** by default — zero setup. A default admin account
(`admin@insurance.com` / `Admin@123`) is seeded automatically on first run, along with
starter Insurance Categories.

- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:insurancedb`, user `sa`, blank password)

**Note:** H2 in-memory data resets every time the backend restarts. To persist data
across restarts locally, change the datasource URL in `application.properties` to:
~~~properties
spring.datasource.url=jdbc:h2:file:./data/insurancedb;DB_CLOSE_ON_EXIT=FALSE
~~~

### Frontend
~~~bash
cd frontend
npm install
npm run dev
~~~
Opens on **http://localhost:5173**, configured via `frontend/.env` to talk to
`http://localhost:8080/api`.

---

## 4. Deployment

This project is deployed using a fully free stack:

| Piece | Service |
|---|---|
| Backend | [Render](https://render.com) — free web service, deployed via Docker |
| Database | [Neon](https://neon.tech) — free permanent PostgreSQL |
| Frontend | [Vercel](https://vercel.com) — free Hobby plan |

### Backend (Render)
- Deploys from `backend/Dockerfile` (multi-stage build: Maven+JDK 21 to compile, JRE 21 to run)
- Root Directory: `backend`
- Environment variables required:
  ~~~
  SPRING_PROFILES_ACTIVE     = prod
  SPRING_DATASOURCE_URL      = jdbc:postgresql://<neon-host>/<db>?sslmode=require
  SPRING_DATASOURCE_USERNAME = <from Neon>
  SPRING_DATASOURCE_PASSWORD = <from Neon>
  APP_JWT_SECRET             = <a long random string>
  CORS_ALLOWED_ORIGINS       = https://*.vercel.app,http://localhost:5173
  ~~~
- `server.port` reads Render's injected `PORT` env var automatically (`${PORT:8080}` in `application.properties`), falling back to 8080 locally.
- CORS uses `setAllowedOriginPatterns` (not `setAllowedOrigins`), so the wildcard
  `https://*.vercel.app` matches every Vercel preview URL without needing updates
  on every deploy.

### Frontend (Vercel)
- Root Directory: `frontend`
- Framework Preset: Vite (auto-detected)
- Environment variable: `VITE_API_URL = https://<your-render-url>/api`

### Known free-tier limitations
- Render's free web service spins down after 15 minutes idle (cold start delay on next request).
- File uploads (`backend/uploads/`) don't persist on Render's free tier — the filesystem is ephemeral and resets on redeploy. For real persistent file storage, this would need S3 or similar.

---

## 5. Key API Endpoints

| Module | Endpoint prefix |
|---|---|
| Auth | `POST /api/auth/register`, `POST /api/auth/login` |
| Account (self-service) | `/api/account/me` |
| Customers | `/api/customers` (`/paged`, `/{id}/assign-agent`) |
| Policies | `/api/policies` (`/paged`, `/{id}/renew`, `/{id}/cancel`) |
| Claims | `/api/claims` (`/paged`, `/{id}/assign`, `/{id}/decision`, `/{id}/documents`) |
| Premiums | `/api/premiums` (`/paged`, `/{id}/pay`) |
| Documents | `/api/documents` (`/paged`, `/{id}/download`) |
| Insurance Categories | `/api/policy-categories` |
| Employees | `/api/employees` (Admin-only) |
| System Settings | `/api/settings` (Admin-only write) |
| Notifications | `/api/notifications/me` |
| Audit Logs | `/api/audit-logs` (Admin-only) |
| Reports | `/api/reports/summary`, `/api/reports/monthly-report/pdf` |

Full interactive documentation available via Swagger UI once the backend is running.

---

## 6. Scheduled Jobs

- **Daily @ midnight** — flips policies past their end date from `ACTIVE` → `EXPIRED`
- **Daily @ 06:00** — sends policy expiry-reminder notifications within the configured window
- **Daily @ 00:30** — flips overdue premium payments to `OVERDUE` and notifies the customer

---

## 7. Notes

This project implements every module from the original specification end-to-end,
plus several extensions: employee management, claim assignment, system settings,
audit logs, notifications, dark mode, structured insurance categories, agent-scoped
visibility, and full pagination/search across all major lists.

Not included: real payment gateway integration (premium "payment" is a status flag,
not an actual transaction), email/SMS notifications (in-app only), and file storage
beyond local disk (no S3/cloud storage integration).
