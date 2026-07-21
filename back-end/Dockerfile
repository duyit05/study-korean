# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
ENV MAVEN_OPTS="-Xms64m -Xmx256m"
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/back-end-0.0.1-SNAPSHOT.jar app.jar
# Tối ưu RAM & Startup cho Render Free Tier (512MB RAM limit)
ENTRYPOINT ["java", "-Xms32m", "-Xmx160m", "-XX:MaxMetaspaceSize=128m", "-XX:ReservedCodeCacheSize=32m", "-Xss256k", "-XX:+UseSerialGC", "-jar", "app.jar"]
