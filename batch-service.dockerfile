# Build stage
FROM gradle:jdk21 AS build

WORKDIR /app

# Copy root gradle files
COPY batch-service/build.gradle batch-service/settings.gradle batch-service/gradlew batch-service/gradlew.bat ./batch-service/
COPY batch-service/gradle ./batch-service/gradle

# Copy module gradle files
COPY batch-service/settlement/build.gradle ./batch-service/settlement/
COPY batch-service/search/build.gradle ./batch-service/search/

# Copy common gradle files
COPY common/build.gradle ./common/

WORKDIR /app/batch-service
RUN chmod +x ./gradlew && \
    ./gradlew dependencies --no-daemon --build-cache

WORKDIR /app
COPY common ./common
COPY observability-config ./observability-config
COPY batch-service ./batch-service

WORKDIR /app/batch-service
# Build root project
RUN ./gradlew build -x test --parallel --no-daemon --build-cache

FROM gcr.io/distroless/java21-debian12

WORKDIR /app

# Copy the batch-service jar (root)
COPY --from=build /app/batch-service/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Xms700m", "-Xmx700m", "-jar", "-Dspring.profiles.active=prod,secret", "app.jar"]
