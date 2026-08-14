# Imagen de runtime solamente - ver config-server/Dockerfile para el porque.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/customer-service.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
