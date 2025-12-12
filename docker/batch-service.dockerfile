# Build stage
FROM gradle:jdk21 AS build

WORKDIR /app

COPY batch-service/build.gradle batch-service/settings.gradle batch-service/gradlew batch-service/gradlew.bat ./batch-service/
COPY batch-service/gradle ./batch-service/gradle

COPY batch-service/settlement/build.gradle ./batch-service/settlement/
COPY batch-service/search/build.gradle ./batch-service/search/
COPY common/build.gradle ./common/

COPY common ./common
COPY observability-config ./observability-config
COPY batch-service ./batch-service

WORKDIR /app/batch-service
RUN sed -i 's/\r$//' ./gradlew
RUN ./gradlew clean build -x test --parallel --no-daemon --build-cache

FROM gcr.io/distroless/java21-debian12

WORKDIR /app

COPY --from=build /app/batch-service/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Xms700m", "-Xmx700m", "-jar", "-Dspring.profiles.active=prod,secret", "app.jar"]
