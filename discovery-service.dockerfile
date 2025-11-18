FROM gradle:jdk21 AS build

WORKDIR /app

COPY discovery-service/build.gradle discovery-service/settings.gradle discovery-service/gradlew discovery-service/gradlew.bat ./discovery-service/
COPY discovery-service/gradle ./discovery-service/gradle


COPY discovery-service/src ./discovery-service/src

WORKDIR /app/discovery-service
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon
RUN ./gradlew clean build -x test --parallel --no-daemon

FROM gcr.io/distroless/java21-debian12

WORKDIR /app

COPY --from=build /app/discovery-service/build/libs/*.jar app.jar


ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
