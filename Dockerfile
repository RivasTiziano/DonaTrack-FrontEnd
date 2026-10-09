# ==========================
# Etapa 1: Build de Maven
# ==========================
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copiar configuración de dependencias y descargarlas
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================
# Etapa 2: Imagen de Producción / Run
# ==========================
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiar el JAR generado
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
