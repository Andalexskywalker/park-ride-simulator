# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . /app
RUN mvn -q -DskipTests -pl eureka-server -am package

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /app/eureka-server/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]