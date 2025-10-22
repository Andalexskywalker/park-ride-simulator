# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn -q -DskipTests -pl service-parques -am clean package

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /opt/app
COPY --from=build /app/service-parques/target/app.jar /opt/app/app.jar
ENV JAVA_TOOL_OPTIONS="-XX:+UseZGC -Xms96m -Xmx192m"
EXPOSE 8081
ENTRYPOINT ["java","-jar","/opt/app/app.jar"]
