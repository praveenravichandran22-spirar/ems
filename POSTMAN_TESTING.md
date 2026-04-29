# EMS API — Postman Testing Guide

Base URL: `http://localhost:8080`

---

## How to Start the App

**Terminal (Maven):**
```powershell
.\mvnw.cmd spring-boot:run
```

**IntelliJ IDEA:**
Click the green ▶ button on `EmsApplication.java` — no extra config needed.

---

## 1. Auth Endpoints (Public — no token needed)

### 1.1 Register Admin

```
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "firstName": "Super",
  "lastName": "Admin",
  "email": "admin@ems.com",
  "password": "admin123",
  "role": "ROLE_ADMIN"
}
```

**Response `201 Created`:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "a3f1c2d4-870b-4f3e-...",
  "email": "admin@ems.com",
  "firstName": "Super",
  "lastName": "Admin",
  "role": "ROLE_ADMIN"
}
```

---

### 1.2 Register Regular User

```
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "firstName": "John",
  "lastName": "Viewer",
  "email": "user@ems.com",
  "password": "user123",
  "role": "ROLE_USER"
}
```

---

### 1.3 Login

```
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "admin@ems.com",
  "password": "admin123"
}
```

**Response `200 OK`:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "a3f1c2d4-870b-4f3e-...",
  "email": "admin@ems.com",
  "firstName": "Super",
  "lastName": "Admin",
  "role": "ROLE_ADMIN"
}
```

> Copy `accessToken` → use in all protected requests via **Authorization → Bearer Token**.
> Copy `refreshToken` separately → needed for `/refresh` and `/logout`.

---

### 1.4 Refresh Access Token

When the access token expires (24h), call this to get a new pair without logging in again.

```
POST /api/auth/refresh
Content-Type: application/json
```

```json
{
  "refreshToken": "a3f1c2d4-870b-4f3e-..."
}
```

**Response `200 OK`** — brand-new `accessToken` + `refreshToken` (old refresh token is invalidated).

**Response `401`** — token expired (7 days) or not found → user must log in again.

---

### 1.5 Logout

Deletes the refresh token from the server so neither token can be reused.

```
POST /api/auth/logout
Content-Type: application/json
```

```json
{
  "refreshToken": "a3f1c2d4-870b-4f3e-..."
}
```

**Response `204 No Content`**

---

## 2. Lookup / Dropdown Endpoints (Public — no token needed)

These are called by the UI on form load to populate dropdowns. Department and status are DB-backed — an admin can add new ones without code changes.

### 2.1 Get All Departments

```
GET /api/departments
```

**Response `200 OK`:**
```json
[
  { "id": 1, "name": "Engineering",     "description": "Software and hardware engineering" },
  { "id": 2, "name": "Human Resources", "description": "People operations and talent management" },
  { "id": 3, "name": "Finance",         "description": "Financial planning and accounting" },
  { "id": 4, "name": "Marketing",       "description": "Brand, growth and communications" },
  { "id": 5, "name": "Sales",           "description": "Revenue generation and client relations" },
  { "id": 6, "name": "Operations",      "description": "Business operations and process management" },
  { "id": 7, "name": "Legal",           "description": "Legal affairs and compliance" },
  { "id": 8, "name": "Design",          "description": "UX, UI and product design" }
]
```

---

### 2.2 Get All Employment Statuses

```
GET /api/statuses
```

**Response `200 OK`:**
```json
[
  { "id": 1, "name": "Active",     "description": "Currently employed and working" },
  { "id": 2, "name": "Inactive",   "description": "Employment paused or not yet started" },
  { "id": 3, "name": "On Leave",   "description": "Temporarily away from work" },
  { "id": 4, "name": "Terminated", "description": "Employment ended" },
  { "id": 5, "name": "Probation",  "description": "Under probationary period" }
]
```

---

### 2.3 Get Genders

```
GET /api/enums/genders
```

**Response `200 OK`:**
```json
["MALE", "FEMALE", "OTHER", "PREFER_NOT_TO_SAY"]
```

---

### 2.4 Add New Department _(ROLE_ADMIN only)_

```
POST /api/departments
Authorization: Bearer <admin-token>
Content-Type: application/json
```

```json
{
  "name": "Data Science",
  "description": "AI, ML and analytics"
}
```

**Response `201 Created`:**
```json
{ "id": 9, "name": "Data Science", "description": "AI, ML and analytics" }
```

---

### 2.5 Update Department _(ROLE_ADMIN only)_

```
PUT /api/departments/{id}
Authorization: Bearer <admin-token>
Content-Type: application/json
```

```json
{
  "name": "Data Science & AI",
  "description": "Artificial intelligence and data analytics"
}
```

---

### 2.6 Delete Department _(ROLE_ADMIN only)_

```
DELETE /api/departments/{id}
Authorization: Bearer <admin-token>
```

**Response `204 No Content`**

> Same CRUD pattern applies to `/api/statuses`.

---

## 3. Employee Endpoints (Token required)

> **Important:** `departmentId` and `statusId` are the numeric IDs from `/api/departments` and `/api/statuses`. Call those first to get the IDs.

### 3.1 Create Employee _(ROLE_ADMIN only)_

```
POST /api/employees
Authorization: Bearer <admin-token>
Content-Type: application/json
```

```json
{
  "firstName": "Ravi",
  "lastName": "Kumar",
  "email": "ravi.kumar@example.com",
  "phone": "9876543210",
  "address": "123 MG Road, Bengaluru",
  "bio": "Senior software engineer with 8 years of experience.",
  "departmentId": 1,
  "statusId": 1,
  "gender": "MALE",
  "salary": 120000.00,
  "experienceYears": 8,
  "rating": 4,
  "isRemote": true,
  "dateOfBirth": "1990-05-15",
  "joiningDate": "2022-01-10"
}
```

**Response `201 Created`:**
```json
{
  "id": 5,
  "firstName": "Ravi",
  "lastName": "Kumar",
  "email": "ravi.kumar@example.com",
  "phone": "9876543210",
  "address": "123 MG Road, Bengaluru",
  "bio": "Senior software engineer with 8 years of experience.",
  "department": { "id": 1, "name": "Engineering", "description": "Software and hardware engineering" },
  "status":     { "id": 1, "name": "Active",      "description": "Currently employed and working" },
  "gender": "MALE",
  "salary": 120000.00,
  "experienceYears": 8,
  "rating": 4,
  "isRemote": true,
  "dateOfBirth": "1990-05-15",
  "joiningDate": "2022-01-10",
  "profileImageUrl": null,
  "resumeUrl": null,
  "createdAt": "2026-04-28T10:00:00",
  "updatedAt": "2026-04-28T10:00:00"
}
```

---

### 3.2 Get All Employees (paginated + sortable)

```
GET /api/employees?page=0&size=10&sortBy=firstName&sortDir=asc
Authorization: Bearer <token>
```

| Query Param | Default | Options |
|-------------|---------|---------|
| `page`      | `0`     | 0-based index |
| `size`      | `10`    | any number |
| `sortBy`    | `id`    | `id`, `firstName`, `lastName`, `salary`, `joiningDate`, `experienceYears`, `rating` |
| `sortDir`   | `asc`   | `asc`, `desc` |

**Response `200 OK`:**
```json
{
  "content": [
    {
      "id": 3,
      "firstName": "Admin",
      "lastName": "EMSSuperBoss",
      "department": { "id": 1, "name": "Engineering", "description": "..." },
      "status":     { "id": 1, "name": "Active",      "description": "..." },
      "..."
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 4,
  "totalPages": 1,
  "last": true
}
```

---

### 3.3 Search & Filter Employees

```
GET /api/employees/search?keyword=ravi&departmentId=1&statusId=1&page=0&size=10&sortBy=salary&sortDir=desc
Authorization: Bearer <token>
```

| Query Param    | Required | Description |
|----------------|----------|-------------|
| `keyword`      | No       | Searches `firstName`, `lastName`, `email` (case-insensitive) |
| `departmentId` | No       | Filter by department ID (from `/api/departments`) |
| `statusId`     | No       | Filter by status ID (from `/api/statuses`) |
| `page`         | No       | Default `0` |
| `size`         | No       | Default `10` |
| `sortBy`       | No       | Default `id` |
| `sortDir`      | No       | Default `asc` |

---

### 3.4 Get Employee by ID

```
GET /api/employees/3
Authorization: Bearer <token>
```

**Response `200 OK`** — single employee object (same shape as create response).
**Response `404`** — `{ "status": 404, "error": "Employee not found with id: 99" }`

---

### 3.5 Update Employee _(ROLE_ADMIN only)_

```
PUT /api/employees/1
Authorization: Bearer <admin-token>
Content-Type: application/json
```

```json
{
  "firstName": "Ravi",
  "lastName": "Kumar",
  "email": "ravi.kumar@example.com",
  "phone": "9123456789",
  "address": "456 Brigade Road, Bengaluru",
  "bio": "Updated bio.",
  "departmentId": 2,
  "statusId": 3,
  "gender": "MALE",
  "salary": 135000.00,
  "experienceYears": 9,
  "rating": 5,
  "isRemote": false,
  "dateOfBirth": "1990-05-15",
  "joiningDate": "2022-01-10"
}
```

---

### 3.6 Delete Employee _(ROLE_ADMIN only)_

```
DELETE /api/employees/1
Authorization: Bearer <admin-token>
```

**Response `204 No Content`** — also deletes profile image and resume files from disk.

---

## 4. File Upload Endpoints _(ROLE_ADMIN only)_

### 4.1 Upload Profile Image

```
POST /api/employees/{id}/profile-image
Authorization: Bearer <admin-token>
Body: form-data
```

| Key    | Type | Value |
|--------|------|-------|
| `file` | File | select a `.jpg` / `.png` |

**Response `200 OK`** — updated employee with `profileImageUrl` populated:
```json
{
  "profileImageUrl": "/api/files/images/uuid_photo.jpg",
  "..."
}
```

---

### 4.2 Upload Resume

```
POST /api/employees/{id}/resume
Authorization: Bearer <admin-token>
Body: form-data
```

| Key    | Type | Value |
|--------|------|-------|
| `file` | File | select a `.pdf` |

**Response `200 OK`** — updated employee with `resumeUrl` populated.

---

## 5. File Access Endpoint (Public — no token needed)

```
GET /api/files/images/{filename}
GET /api/files/resumes/{filename}
```

Use the `profileImageUrl` / `resumeUrl` values from any employee response directly in the browser or Postman.

**Examples:**
```
GET http://localhost:8080/api/files/images/3f8a2b1c_photo.jpg
GET http://localhost:8080/api/files/resumes/9d4e7f0a_resume.pdf
```

---

## 6. Access Control Matrix

| Endpoint | No Token | ROLE_USER | ROLE_ADMIN |
|----------|----------|-----------|------------|
| `POST /api/auth/**`               | ✅ | ✅ | ✅ |
| `GET /api/departments`            | ✅ | ✅ | ✅ |
| `GET /api/statuses`               | ✅ | ✅ | ✅ |
| `GET /api/enums/**`               | ✅ | ✅ | ✅ |
| `GET /api/files/**`               | ✅ | ✅ | ✅ |
| `POST/PUT/DELETE /api/departments`| ❌ 401 | ❌ 403 | ✅ |
| `POST/PUT/DELETE /api/statuses`   | ❌ 401 | ❌ 403 | ✅ |
| `GET /api/employees`              | ❌ 401 | ✅ 200 | ✅ 200 |
| `GET /api/employees/{id}`         | ❌ 401 | ✅ 200 | ✅ 200 |
| `GET /api/employees/search`       | ❌ 401 | ✅ 200 | ✅ 200 |
| `POST /api/employees`             | ❌ 401 | ❌ 403 | ✅ 201 |
| `PUT /api/employees/{id}`         | ❌ 401 | ❌ 403 | ✅ 200 |
| `DELETE /api/employees/{id}`      | ❌ 401 | ❌ 403 | ✅ 204 |
| `POST /api/employees/{id}/profile-image` | ❌ 401 | ❌ 403 | ✅ 200 |
| `POST /api/employees/{id}/resume` | ❌ 401 | ❌ 403 | ✅ 200 |

---

## 7. Seeded Data Reference

Use these IDs when creating or updating employees.

### Departments

| ID | Name |
|----|------|
| 1  | Engineering |
| 2  | Human Resources |
| 3  | Finance |
| 4  | Marketing |
| 5  | Sales |
| 6  | Operations |
| 7  | Legal |
| 8  | Design |

### Employment Statuses

| ID | Name |
|----|------|
| 1  | Active |
| 2  | Inactive |
| 3  | On Leave |
| 4  | Terminated |
| 5  | Probation |

### Existing Users & Employees

| User | Role | Employee ID | Department | Status |
|------|------|-------------|-----------|--------|
| admin@ems.com | ROLE_ADMIN | 3 | Engineering (1) | Active (1) |
| heisen@ems.com | ROLE_USER | 4 | Operations (6) | Active (1) |

---

## 8. Valid Enum Values

### Gender
```
MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
```

### Role (register only)
```
ROLE_ADMIN, ROLE_USER
```

---

## 9. Error Response Format

```json
{
  "timestamp": "2026-04-28T10:00:00",
  "status": 404,
  "error": "Employee not found with id: 99"
}
```

| Status | Cause |
|--------|-------|
| `400`  | Bad request (duplicate email, duplicate department name) |
| `401`  | Missing/invalid/expired token |
| `403`  | Valid token but insufficient role |
| `404`  | Employee / Department / Status not found |
| `500`  | Unexpected server error |
