# Build stage
FROM gradle:jdk21 AS build

WORKDIR /app

# Copy root gradle files
COPY build.gradle settings.gradle gradlew gradlew.bat ./
COPY gradle ./gradle

# Copy module gradle files
COPY ai-service/build.gradle ./ai-service/

# Copy config gradle files (dependencies)
COPY observability-config/build.gradle ./observability-config/
COPY observability-config/zipkin/build.gradle ./observability-config/zipkin/

WORKDIR /app
RUN chmod +x ./gradlew && \
    ./gradlew dependencies --no-daemon --build-cache

COPY observability-config ./observability-config
COPY ai-service ./ai-service

WORKDIR /app/ai-service
# Build project
RUN sed -i 's/\r$//' ../gradlew
RUN ../gradlew build -x test --parallel --no-daemon --build-cache

FROM gcr.io/distroless/java21-debian12

WORKDIR /app

# Copy the jar
COPY --from=build /app/ai-service/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Xms700m", "-Xmx700m", "-jar", "-Dspring.profiles.active=prod,secret", "app.jar"]
