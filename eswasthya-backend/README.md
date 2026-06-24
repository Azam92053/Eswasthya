# eSwasthya — Backend API

A lightweight Spring Boot REST API for personal health tracking. Record BMI, blood pressure, glucose, and activity metrics; receive automated alerts; and inspect aggregated admin statistics.

Quick facts
- Java 21 · Spring Boot 3.4.2
- MySQL 8.x (production), H2 for tests
- JWT authentication (jjwt)
- Spring Data JPA, Validation, Actuator
- Maven (wrapper included), Docker-friendly

Quickstart
1. Provide required environment variables:
   - SPRING_DATASOURCE_URL
   - SPRING_DATASOURCE_USERNAME
   - SPRING_DATASOURCE_PASSWORD
   - APP_JWT_SECRET

2. Build (Windows):
   .\mvnw.cmd clean package -DskipTests

   Build (Linux/macOS):
   ./mvnw clean package -DskipTests

3. Run:
   java -jar target/eswasthya-backend-1.0.0.jar

Docker
- Build: docker build -t eswasthya-backend .
- Run:
  docker run --rm -p 8080:8080 \
    -e SPRING_DATASOURCE_URL='jdbc:mysql://...' \
    -e SPRING_DATASOURCE_USERNAME=root \
    -e SPRING_DATASOURCE_PASSWORD=secret \
    -e APP_JWT_SECRET='replace-me' \
    eswasthya-backend

Configuration
- All runtime configuration is read from src/main/resources/*.properties and environment variables. In production provide DB credentials and JWT secret via env vars.
- Actuator health is enabled (management.endpoint.health).

API (summary)
- Auth: POST /api/auth/register, POST /api/auth/login
- Health records: POST/GET/PUT/DELETE /api/health/records
- Dashboard & report: GET /api/health/dashboard, /api/health/report
- Alerts: GET /api/alerts, PUT /api/alerts/{id}/read
- Admin: GET /api/admin/stats, /api/admin/users

Testing
- Run unit tests: .\mvnw.cmd test (uses H2 in-memory DB)

Contributing
- Fork the repo, create a feature branch, include tests for new behavior, and open a pull request. Keep commits atomic and descriptive.

License
- MIT. See LICENSE file.

Contact
- Author: Azam Khan

---
Notes
- This README is intentionally concise. For full API details consult the controller classes under src/main/java/com/eswasthya/controller and DTOs under dto/.
