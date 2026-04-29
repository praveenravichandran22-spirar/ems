# Employee Management System — Backend

REST API backend for the Employee Management System, built with **Spring Boot 4**, **Spring Security (JWT)**, **JPA/Hibernate**, and **PostgreSQL**.

---

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Language |
| Spring Boot | 4.0.6 | Framework |
| Spring Security | 6.x | Authentication & Authorization |
| Spring Data JPA | — | ORM / database access |
| Hibernate | 6.x | JPA implementation |
| PostgreSQL | latest | Primary database |
| jjwt | 0.12.6 | JWT token generation & validation |
| Lombok | latest | Boilerplate reduction |
| Maven | 3.x | Build tool |
| Docker | — | PostgreSQL + pgAdmin containerization |

---

## Features

- **JWT Authentication** — stateless access + refresh token flow
- **Role-based Access Control** — `ROLE_ADMIN` and `ROLE_USER` with method-level security
- **BCrypt Password Encryption** — passwords never stored in plain text
- **Full CRUD** — employees, departments, employment statuses
- **Search & Pagination** — keyword search, department/status filters, column sorting, page size control
- **Binary File Storage** — profile images and resumes stored as `bytea` blobs directly in PostgreSQL
- **User Management** — admin-only endpoint to list and filter registered users
- **Dataset Simulation** — 2,000 seeded sim-employee records for pagination/search performance testing
- **CORS** — pre-configured for Angular dev server (`localhost:4200`)

---

## Project Structure

```
src/main/java/com/minifullstack/ems/
├── config/
│   ├── SecurityConfig.java          # JWT filter chain, BCrypt bean, CORS
│   ├── DataInitializer.java         # Seeds default admin user + lookup data
│   └── SimDataSeeder.java           # Seeds 2,000 simulation records
├── controller/
│   ├── AuthController.java          # POST /api/auth/** — login, register, refresh, logout
│   ├── EmployeeController.java      # /api/employees — CRUD + file upload/serve
│   ├── DepartmentController.java    # /api/departments
│   ├── EmploymentStatusController.java  # /api/statuses
│   ├── UserController.java          # /api/users — admin only
│   ├── SimEmployeeController.java   # /api/sim-employees/search
│   └── FileController.java          # /api/files/** — static file fallback
├── security/
│   ├── JwtUtils.java                # Token generation, parsing, validation
│   └── JwtAuthFilter.java           # Per-request Bearer token filter
├── service/
│   ├── AuthService.java / impl      # Login, register, refresh, logout logic
│   ├── EmployeeService.java / impl  # Employee CRUD, file blob storage
│   ├── UserManagementService.java / impl
│   └── SimEmployeeService.java / impl
├── repository/
│   ├── EmployeeRepository.java      # JPQL search, excludes registered user emails
│   ├── UserRepository.java          # searchAll + searchByRole (two methods)
│   └── SimEmployeeRepository.java   # JPQL search with DB indexes
├── entity/
│   ├── Employee.java                # Full profile + bytea blob columns
│   ├── User.java                    # Auth user with role enum
│   ├── SimEmployee.java             # Flat sim table, indexed columns
│   ├── Department.java
│   └── EmploymentStatus.java
├── dto/
│   ├── request/                     # Incoming request DTOs
│   └── response/                    # Outgoing response DTOs (no password fields)
└── exception/
    └── ResourceNotFoundException.java
```

---

## API Reference

### Auth — `/api/auth`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register a new user |
| POST | `/api/auth/login` | Public | Login, returns access + refresh token |
| POST | `/api/auth/refresh` | Public | Get new access token via refresh token |
| POST | `/api/auth/logout` | Public | Invalidate refresh token |

### Employees — `/api/employees`

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/employees/search` | Authenticated | Search with keyword, dept, status, pagination, sort |
| GET | `/api/employees/{id}` | Authenticated | Get employee by ID |
| POST | `/api/employees` | Admin | Create employee |
| PUT | `/api/employees/{id}` | Admin | Update employee |
| DELETE | `/api/employees/{id}` | Admin | Delete employee |
| POST | `/api/employees/{id}/profile-image` | Admin | Upload profile image (stored as blob) |
| POST | `/api/employees/{id}/resume` | Admin | Upload resume (stored as blob) |
| GET | `/api/employees/{id}/profile-image` | Public | Serve profile image from DB |
| GET | `/api/employees/{id}/resume` | Authenticated | Download resume from DB |

### Lookups

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/departments` | Public | List all departments |
| GET | `/api/statuses` | Public | List all employment statuses |
| GET | `/api/enums/genders` | Public | List gender enum values |

### Admin

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/users` | Admin | Search registered users with pagination |
| GET | `/api/sim-employees/search` | Authenticated | Search 2,000-record simulation dataset |

---

## Getting Started

### Prerequisites

- Java 21
- Maven 3.x
- Docker & Docker Compose

### 1. Start PostgreSQL (and pgAdmin)

```bash
docker-compose up -d
```

This starts:
- **PostgreSQL** on `localhost:5432`
- **pgAdmin** on `http://localhost:5050` (email: `admin@ems.com` / password: `admin@123`)

### 2. Configure `application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ems_db
spring.datasource.username=admin
spring.datasource.password=admin@123

app.jwt.secret=<your-256-bit-secret>
app.jwt.expiration-ms=86400000
app.jwt.refresh-expiration-ms=604800000
```

### 3. Run the application

```bash
mvn spring-boot:run
```

The API starts on **`http://localhost:8080`**.

On first startup, `DataInitializer` seeds:
- A default **admin** user (`admin@ems.com` / `Admin@123`)
- Default departments and employment statuses

`SimDataSeeder` seeds **2,000 simulation employees** (skips if already present).

---

## Authentication Flow

```
POST /api/auth/login
  → returns { accessToken, refreshToken }

All protected requests:
  Authorization: Bearer <accessToken>

When access token expires:
POST /api/auth/refresh  { refreshToken }
  → returns new { accessToken, refreshToken }
```

---

## File Storage

Profile images and resumes are stored as **binary blobs (`bytea`) in PostgreSQL** — no filesystem dependency.

| Column | Type | Description |
|---|---|---|
| `profile_image_data` | `bytea` | Raw image bytes |
| `profile_image_content_type` | `varchar` | e.g. `image/jpeg` |
| `resume_data` | `bytea` | Raw document bytes |
| `resume_content_type` | `varchar` | e.g. `application/pdf` |
| `resume_file_name` | `varchar` | Original filename for download |

Serving endpoints stream the bytes with the correct `Content-Type` header. Profile images are served publicly (used in `<img>` tags). Resumes require authentication and are served with `Content-Disposition: attachment`.

---

## Security Rules Summary

| Endpoint pattern | Access |
|---|---|
| `/api/auth/**` | Public |
| `/api/files/**` | Public |
| `/api/departments/**` (GET) | Public |
| `/api/statuses/**` (GET) | Public |
| `/api/employees/*/profile-image` (GET) | Public |
| `/api/employees/**` (GET) | Authenticated |
| `/api/employees/**` (POST, PUT, DELETE) | Admin only |
| `/api/sim-employees/**` (GET) | Authenticated |
| `/api/users/**` (GET) | Admin only |

---

## Environment Variables / Properties

| Property | Default | Description |
|---|---|---|
| `spring.datasource.url` | — | PostgreSQL JDBC URL |
| `spring.datasource.username` | — | DB username |
| `spring.datasource.password` | — | DB password |
| `spring.jpa.hibernate.ddl-auto` | `update` | Schema management (`update` auto-applies entity changes) |
| `app.jwt.secret` | — | 256-bit hex secret for signing JWTs |
| `app.jwt.expiration-ms` | `86400000` | Access token TTL (24 hours) |
| `app.jwt.refresh-expiration-ms` | `604800000` | Refresh token TTL (7 days) |
| `spring.servlet.multipart.max-file-size` | `10MB` | Max upload size |
