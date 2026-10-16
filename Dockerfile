# Multi-stage build for Jana Address Spring Boot Service
FROM docker.io/library/maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /build

COPY backend/pom.xml .
COPY backend/src ./src

# Build production JAR skipping test execution in container
RUN mvn clean package -DskipTests

# Stage 2: Minimal, fast, secure Alpine JRE 21 runtime
FROM docker.io/library/eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=build /build/target/jana-address-*.jar /app/app.jar

EXPOSE 8000

ENV JAVA_OPTS="-XX:+UseParallelGC -Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
