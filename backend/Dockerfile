# --- Build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy just the pom first so dependency downloads are cached across builds
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/insurance-management-platform.jar app.jar

# Render injects a PORT environment variable at runtime; application.properties
# reads it via server.port=${PORT:8080} (falls back to 8080 for local Docker runs).
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]