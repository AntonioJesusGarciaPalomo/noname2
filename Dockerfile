FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copiar archivos del proyecto
COPY .mvn/ .mvn/
COPY mvnw mvnw.cmd pom.xml ./

# Dar permisos de ejecución al script de Maven
RUN chmod +x ./mvnw

# Descargar dependencias (para aprovechar la caché de Docker)
RUN ./mvnw dependency:go-offline

# Copiar el código fuente
COPY src/ src/

# Compilar la aplicación
RUN ./mvnw package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiar el archivo JAR construido
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto 3000
EXPOSE 3000

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]