# Insurance Management Platform

A full-stack Insurance Management Platform built as an internship-style project.
It covers customer management, policy management, claims, premium tracking,
document management, and a reports dashboard — with role-based access for
**Admin**, **Agent**, and **Customer** users.

- **Backend:** Java 21, Spring Boot 3, Spring Security + JWT, Spring Data JPA (Hibernate), H2 (default) / PostgreSQL, OpenPDF, SpringDoc OpenAPI (Swagger)
- **Frontend:** React.js (Vite), Tailwind CSS, Axios, React Router, Chart.js

---

## 1. Project structure

```
insurance-management-platform/
├── backend/     Spring Boot API (Maven project — open this folder in IntelliJ)
└── frontend/    React + Vite client
```

## 2. Running the backend (IntelliJ IDEA)

1. Open IntelliJ IDEA → **File → Open** → select the `backend` folder.
2. Let IntelliJ import the Maven project and download dependencies (requires internet access).
3. Make sure the project SDK is **Java 21** (File → Project Structure → SDK).
4. Run `InsuranceManagementPlatformApplication.java` (right-click → Run), or from a terminal:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
5. The API starts on **http://localhost:8080**.

The app ships with an in-memory **H2 database** by default — no setup required.
A default administrator account is auto-created on first run:

```
email:    admin@insurance.com
password: Admin@123
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:insurancedb`, user `sa`, empty password)

### Switching to PostgreSQL

Open `backend/src/main/resources/application.properties`, comment out the H2 block,
and uncomment the PostgreSQL block (update username/password/database name to match
your local Postgres instance). Then add the `postgresql` JDBC driver is already on
the classpath — no code changes needed.

## 3. Running the frontend

```bash
cd frontend
npm install
npm run dev
```

The app starts on **http://localhost:5173** and talks to the backend at
`http://localhost:8080/api` (configurable in `frontend/.env`).

## 4. Roles & what each can do

| Role | Capabilities |
|---|---|
| **Admin** | Everything an Agent can do, plus: manage Agent/Admin employee accounts, assign claims to specific agents, configure system-wide settings, delete customers |
| **Agent** | Register customers, create/renew/cancel policies, verify & decide claims, schedule/track premiums, review documents, view reports |
| **Customer** | Register/login, view own policies, submit claims, pay premiums, upload/download documents, receive in-app notifications |

New users who self-register through the frontend's **Register** page are always
created as **Customer** accounts. Admin/Agent accounts are created by an existing
Admin through the **Employees** page (Admin-only) — this is intentional so the
public registration form can't be used to create privileged accounts.

## 5. Key API endpoints

| Module | Endpoint prefix |
|---|---|
| Auth | `POST /api/auth/register`, `POST /api/auth/login` |
| Customers | `/api/customers` |
| Policies | `/api/policies` |
| Claims | `/api/claims` (includes `/{id}/assign`, `/assigned/me`) |
| Premiums | `/api/premiums` |
| Documents | `/api/documents` (includes `GET /api/documents` for the admin/agent review list) |
| Reports | `/api/reports/summary`, `/api/reports/monthly-report/pdf` |
| Employees | `/api/employees` (Admin-only — manage Agent/Admin accounts) |
| System Settings | `/api/settings` (Admin-only write, Admin/Agent read) |
| Notifications | `/api/notifications/me`, `/api/notifications/{id}/read`, `/api/notifications/read-all` |

Full interactive documentation is available in Swagger UI once the backend is running.

## 6. Scheduled jobs & notifications

- Policies past their end date are automatically flipped from `ACTIVE` → `EXPIRED` daily at midnight.
- Policies nearing their end date (within the configurable "expiry reminder" window in **Settings**)
  trigger an in-app notification for the customer and the agent who created the policy — once per policy.
- Premium payments past their due date + configured grace period are automatically flipped to
  `OVERDUE` daily at 00:30, and the customer receives an in-app notification.
- Notifications are visible via the bell icon in the frontend header for any logged-in user, and are
  fetched via `/api/notifications/me`.

## 7. Admin-only modules

- **Employee Management** (`/employees` in the frontend) — create, edit, enable/disable, or remove
  Agent and Admin accounts. Safeguards prevent removing your own account or the last remaining admin.
- **Claim Assignment** — from the Claims page, an Admin can assign any claim to a specific active agent
  via a dropdown; agents can also fetch their assigned claims via `/api/claims/assigned/me`.
- **System Settings** (`/settings` in the frontend) — company details plus the automation rules that
  drive the scheduled jobs above (default policy term, expiry reminder window, premium grace period).
- **Document Review** (`/documents` in the frontend, shared by Admin & Agent) — a consolidated view of
  every document uploaded across all customers and claims, filterable by document type.

## 8. File uploads

Uploaded documents are stored under `backend/uploads/`. This folder is created
automatically the first time a file is uploaded.

## 8a. Agent-scoped visibility

Every `Customer` record tracks `registeredByAgentEmail` — whoever (Agent or
Admin) registered them. This drives visibility across the app:

- **Admins** see every customer, policy, claim, premium, and document, unrestricted.
- **Agents** only see customers they personally registered, plus the policies,
  claims, premiums, and documents that belong to those customers.
- **Exception:** if an Admin explicitly assigns a claim to an Agent (Claim
  Assignment), that Agent can view/act on it even for a customer they didn't
  register themselves — this models a deliberate hand-off, not a loophole.
- Attempting to access something outside an Agent's scope (e.g. guessing an ID)
  returns `403 Forbidden` rather than silently exposing another Agent's data.

This is enforced at the repository query level (list/search/pagination results
are filtered server-side) and at the service level (individual reads/updates
check ownership before proceeding) — not just hidden in the UI.

## 9. Notes / next steps

This project implements every module from the specification end-to-end, including
Customer/Policy/Claim/Premium/Document management, the Reports Dashboard, and all
three user roles with their full listed responsibilities (employee management, claim
assignment, system settings for Admins included).

Additional features layered on top of the original spec:
- **Self-service profile editing** — `/profile` page for every role (edit name/email,
  change password); customers additionally edit their phone/address/DOB.
- **Audit Logs** — `/audit-logs` (Admin-only) records who did what and when across
  customers, policies, claims, premiums, documents, employees, and settings.
- **Dark Mode** — a sun/moon toggle in the header, persisted to the browser and
  applied via Tailwind's class-based dark mode strategy.
- **Multiple Insurance Categories** — `/policy-categories` (Admin-only) lets you manage
  a structured list of categories (Health, Life, Motor, etc.) instead of free-text;
  the policy creation form uses a dropdown populated from active categories.
- **Search, filter & pagination** — Customers, Policies, Claims, Premiums, and Documents
  all support paginated, filterable listing (`GET .../paged` endpoints with `page`,
  `size`, `search`, and `status`/`type` query params).

The "bonus" features listed in the original brief (email notifications via SMTP,
Excel export, OCR, Redis caching, Docker, CI/CD, etc.) are still **not** included, but
the codebase remains structured (services/controllers/DTOs cleanly separated) to make
adding them straightforward if you want to extend it further.

