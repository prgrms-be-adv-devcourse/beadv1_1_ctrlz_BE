# Build stage
FROM gradle:jdk21 AS build

WORKDIR /app

COPY discovery-service/build.gradle discovery-service/settings.gradle discovery-service/gradlew discovery-service/gradlew.bat ./discovery-service/
COPY discovery-service/gradle ./discovery-service/gradle
COPY discovery-service/gradlew discovery-service/gradlew.bat ./discovery-service/

COPY observability-config ./observability-config
COPY discovery-service/src ./discovery-service/src

WORKDIR /app/discovery-service
RUN sed -i 's/\r$//' ./gradlew
RUN ./gradlew clean build -x test --parallel --no-daemon --build-cache

FROM gcr.io/distroless/java21-debian12

WORKDIR /app

COPY --from=build /app/discovery-service/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Xms700m", "-Xmx700m", "-jar", "-Dspring.profiles.active=prod,secret", "app.jar"]
