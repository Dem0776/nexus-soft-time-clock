# =====================================================================
# Backend — build multi-stage (Java 21). Imagen final JRE slim.
# Contexto de build: raíz del repo (usa backend/ y db/migration).
# =====================================================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Cachea dependencias primero
COPY backend/pom.xml backend/pom.xml
COPY backend/shared-kernel/pom.xml backend/shared-kernel/pom.xml
COPY backend/platform/pom.xml backend/platform/pom.xml
COPY backend/bootstrap/pom.xml backend/bootstrap/pom.xml
RUN cd backend && mvn -q -B dependency:go-offline || true

# Copia fuentes y migraciones (fuente única de db/migration)
COPY backend backend
COPY db db
RUN cd backend && mvn -q -B -DskipTests package

# --- Runtime ---------------------------------------------------------
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
RUN groupadd -r nexus && useradd -r -g nexus nexus
COPY --from=build /workspace/backend/bootstrap/target/bootstrap-*.jar app.jar
USER nexus
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
