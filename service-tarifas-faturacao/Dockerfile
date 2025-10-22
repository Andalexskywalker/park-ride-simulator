# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . /app
RUN mvn -q -DskipTests -pl service-tarifas-faturacao -am clean package spring-boot:repackage

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /app/service-tarifas-faturacao/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]