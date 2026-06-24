# Build the Spring Boot API with Maven and run it on a slim JRE.
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY .mvn/             .mvn/
COPY mvnw              mvnw
COPY pom.xml           pom.xml
RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -q

COPY src/ src/
RUN ./mvnw package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S eswasthya && adduser -S eswasthya -G eswasthya
USER eswasthya

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
