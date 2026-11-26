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
# Agente disponible para activar trazas en Render mediante JAVA_TOOL_OPTIONS.
# Ese agente observa automáticamente las peticiones que recibe Spring y las llamadas HTTP al backend. 
# Permite registrar sus tiempos, errores y relaciones sin agregar código a cada controller o service.
ADD https://repo.maven.apache.org/maven2/io/opentelemetry/javaagent/opentelemetry-javaagent/2.22.0/opentelemetry-javaagent-2.22.0.jar /opt/opentelemetry-javaagent.jar
WORKDIR /app

# Copiar el JAR generado
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
