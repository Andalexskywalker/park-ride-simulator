# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . /app
RUN mvn -q -DskipTests -pl service-tarifas-faturacao -am clean package spring-boot:repackage

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /opt/app
COPY --from=build /app/service-tarifas-faturacao/target/*.jar app.jar
ENV JAVA_TOOL_OPTIONS="-XX:+UseSerialGC -Xss256k -Xmx192m"
EXPOSE 8084
HEALTHCHECK --interval=30s --timeout=3s CMD wget -q -O - http://localhost:8084/actuator/health || exit 1
ENTRYPOINT ["java","-jar","app.jar"]