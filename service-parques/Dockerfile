# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn -q -DskipTests -pl service-parques -am clean package

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /opt/app
COPY --from=build /app/service-parques/target/app.jar /opt/app/app.jar
ENV JAVA_TOOL_OPTIONS="-XX:+UseSerialGC -Xss256k -Xmx192m"
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=3s CMD wget -q -O - http://localhost:8081/actuator/health || exit 1
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
