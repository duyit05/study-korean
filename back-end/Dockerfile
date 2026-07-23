# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
ENV MAVEN_OPTS="-Xms256m -Xmx1g"
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/back-end-0.0.1-SNAPSHOT.jar app.jar
# Tối ưu cho VPS RAM >= 2GB (Khởi động nhanh, GC mặc định)
ENTRYPOINT ["java", "-Xms128m", "-Xmx1g", "-jar", "app.jar"]
