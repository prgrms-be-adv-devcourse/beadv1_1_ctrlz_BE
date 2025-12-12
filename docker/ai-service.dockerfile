# Build stage
FROM gradle:jdk21 AS build

WORKDIR /app

COPY ai-service/build.gradle  ai-service/settings.gradle  ai-service/gradlew ai-service/gradlew.bat ./ai-service/
COPY ai-service/gradle ./ai-service//gradle
COPY ai-service/build.gradle ./ai-service/

COPY observability-config ./observability-config
COPY ai-service ./ai-service

WORKDIR /app/ai-service
# Build project
RUN sed -i 's/\r$//' ./gradlew
RUN ./gradlew clean build -x test --parallel --no-daemon --build-cache

FROM gcr.io/distroless/java21-debian12

WORKDIR /app

# Copy the jar
COPY --from=build /app/ai-service/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Xms700m", "-Xmx700m", "-jar", "-Dspring.profiles.active=prod,secret", "app.jar"]
