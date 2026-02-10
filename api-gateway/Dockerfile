# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . /app
RUN mvn -q -DskipTests -pl api-gateway -am clean package spring-boot:repackage

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /opt/app
COPY --from=build /app/api-gateway/target/*.jar app.jar
ENV JAVA_TOOL_OPTIONS="-XX:+UseSerialGC -Xss256k -Xmx320m"
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s CMD wget -q -O - http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java","-jar","app.jar"]