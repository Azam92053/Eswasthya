# eSwasthya

eSwasthya is a full-stack health and welfare management system with a Spring Boot REST API, a React web application, and a JavaFX desktop client. The platform helps users record BMI, blood pressure, glucose, and activity data, view health trends, receive automated alerts, and manage platform-wide records through an admin dashboard.

## Applications

| App | Folder | Description |
| --- | --- | --- |
| Backend API | `eswasthya-backend` | Spring Boot REST API with JWT authentication, MySQL persistence, health records, alerts, reports, and admin endpoints. |
| Web Frontend | `eswasthya-frontend` | React + Vite client for users and admins. Uses `/api` proxying during local development. |
| Desktop Client | `eswasthya-desktop` | JavaFX desktop application that connects to the same backend API. |

## Tech Stack

| Layer | Technology |
| --- | --- |
| Backend | Java 21, Spring Boot 3.4.2, Spring Security, Spring Data JPA, JWT, MySQL |
| Frontend | React 18, Vite, Axios, React Router, Recharts |
| Desktop | Java 21, JavaFX 21, Maven, Jackson |
| Build Tools | Maven Wrapper, npm |
| Deployment | Dockerfiles for backend and frontend |

## Project Structure

```text
eSwasthya/
+-- eswasthya-backend/     # Spring Boot REST API
+-- eswasthya-frontend/    # React web client
+-- eswasthya-desktop/     # JavaFX desktop client
+-- .gitignore
`-- README.md
```

## Prerequisites

- Git
- Java 21 or later
- Node.js 20 or later
- npm
- MySQL 8.x

The backend and desktop projects include Maven Wrapper scripts, so a separate Maven installation is optional.

## Database Setup

Create the MySQL database before starting the backend:

```sql
CREATE DATABASE eswasthya_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Update the local database credentials in:

```text
eswasthya-backend/src/main/resources/application.properties
```

```properties
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```

## Running Locally

Start the backend first. Both the frontend and desktop client depend on the API running at `http://localhost:8080`.

### 1. Backend API

```powershell
cd eswasthya-backend
.\mvnw.cmd spring-boot:run
```

For macOS or Linux:

```bash
cd eswasthya-backend
./mvnw spring-boot:run
```

The API runs on:

```text
http://localhost:8080
```

Health check:

```text
http://localhost:8080/actuator/health
```

### 2. Web Frontend

Open a second terminal:

```powershell
cd eswasthya-frontend
npm install
npm run dev
```

The web app runs on:

```text
http://localhost:5173
```

During development, Vite proxies `/api` requests to `http://localhost:8080`.

### 3. Desktop Application

Open a third terminal:

```powershell
cd eswasthya-desktop
.\mvnw.cmd javafx:run
```

For macOS or Linux:

```bash
cd eswasthya-desktop
./mvnw javafx:run
```

The desktop client uses the backend base URL configured in:

```text
eswasthya-desktop/src/main/java/com/eswasthya/desktop/EswasthyaDesktopApp.java
```

Default value:

```java
public static final String BASE_URL = "http://localhost:8080";
```

## Build Commands

### Backend

```powershell
cd eswasthya-backend
.\mvnw.cmd clean package
```

Output:

```text
eswasthya-backend/target/*.jar
```

### Frontend

```powershell
cd eswasthya-frontend
npm run build
```

Output:

```text
eswasthya-frontend/dist/
```

### Desktop

```powershell
cd eswasthya-desktop
.\mvnw.cmd clean package
```

Output:

```text
eswasthya-desktop/target/eswasthya-desktop-1.0.0.jar
```

Run the packaged desktop application:

```powershell
java -jar target\eswasthya-desktop-1.0.0.jar
```

## Testing

Backend tests:

```powershell
cd eswasthya-backend
.\mvnw.cmd test
```

Frontend production build check:

```powershell
cd eswasthya-frontend
npm run build
```

Desktop build check:

```powershell
cd eswasthya-desktop
.\mvnw.cmd clean package
```

## Main Features

- User registration and JWT-based login
- Role-based access for students, employees, and admins
- Health record tracking for BMI, blood pressure, glucose, activity level, and notes
- Dashboard summaries and trend charts
- Automated alerts for abnormal health metrics
- Health report download
- Admin dashboard with users, records, and aggregate health statistics
- External health reference endpoints
- Web and desktop clients using the same backend API

## API Overview

| Area | Endpoints |
| --- | --- |
| Authentication | `POST /api/auth/register`, `POST /api/auth/login` |
| User Profile | `GET /api/users/profile`, `PATCH /api/users/profile` |
| Health Records | `POST /api/health/records`, `GET /api/health/records`, `PUT /api/health/records/{id}`, `DELETE /api/health/records/{id}` |
| Dashboard & Reports | `GET /api/health/dashboard`, `GET /api/health/report` |
| Alerts | `GET /api/alerts`, `GET /api/alerts/unread`, `GET /api/alerts/count`, `PUT /api/alerts/{id}/read`, `PUT /api/alerts/read-all` |
| Admin | `GET /api/admin/stats`, `GET /api/admin/users`, `GET /api/admin/users/{id}`, `GET /api/admin/health-records` |
| Health Reference | `GET /api/health/external/topics`, `GET /api/health/external/bmi`, `GET /api/health/external/nutrition` |

Authenticated requests must include:

```http
Authorization: Bearer <jwt_token>
```

## Docker

The backend and frontend include Dockerfiles. There is currently no root-level Docker Compose file, so containers should be connected manually with a shared Docker network or deployed through your own compose configuration.

Build the backend image:

```powershell
docker build -t eswasthya-backend .\eswasthya-backend
```

Run the backend container with MySQL running on the host:

```powershell
docker run --name backend --rm -p 8080:8080 `
  -e SPRING_PROFILES_ACTIVE=docker `
  -e SPRING_DATASOURCE_URL="jdbc:mysql://host.docker.internal:3306/eswasthya_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true" `
  -e SPRING_DATASOURCE_USERNAME=root `
  -e SPRING_DATASOURCE_PASSWORD=your_mysql_password `
  eswasthya-backend
```

Build the frontend image:

```powershell
docker build -t eswasthya-frontend .\eswasthya-frontend
```

The frontend Nginx configuration proxies `/api` to a container named `backend`, so use that name or adjust `eswasthya-frontend/nginx.conf` for your deployment.

## GitHub Checklist

Before pushing:

- Keep database passwords, JWT secrets, and local environment values out of public commits.
- Do not commit generated folders such as `target/`, `dist/`, or `node_modules/`. Add these paths to your `.gitignore`.
- If generated artifacts or secrets were already committed, untrack and remove them and then commit the removal. Example:

```bash
# add patterns to .gitignore first, then:
git rm -r --cached eswasthya-backend/target eswasthya-frontend/dist eswasthya-frontend/node_modules eswasthya-desktop/target
git commit -m "Remove generated build artifacts from repository"
```

- If you accidentally committed secrets (database passwords, JWT keys, API keys, etc.), rotate the secrets immediately and consider removing them from git history using tools like `git filter-repo` or the BFG Repo-Cleaner. Note: rewriting history requires a force-push and coordination with collaborators.
- Run the backend tests and frontend build check.
- Confirm the backend starts successfully with your local MySQL credentials.

## Licenses

- Root: This repository is licensed under the MIT License — see `LICENSE` at the repository root.
- Backend: a license file exists at `eswasthya-backend/LICENSE`. If a module uses a different license, that file takes precedence for that module.

If you prefer a different license, tell me which one and I can replace the root `LICENSE` accordingly.
