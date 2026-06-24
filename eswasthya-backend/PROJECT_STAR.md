# eSwasthya — Project Story (STAR)

Situation
- Individual health data was fragmented and hard to track; no lightweight, privacy-minded backend existed to record personal health metrics and surface alerts.

Task
- Deliver a secure, production-ready REST API so users can register/login, store health records (BMI, BP, glucose, activity), receive alerts, and let admins view aggregated stats.

Action
- Implemented a Spring Boot backend (controllers, services, DTOs, JPA entities), JWT auth, global exception handling, Actuator health, Dockerfile, and environment-driven configuration. Removed IDE/build artifacts and replaced hard-coded secrets with env variables.

Result
- A deployable, Docker-ready backend with authentication, record CRUD, alerts, external health references, and safer production configuration (no embedded secrets).