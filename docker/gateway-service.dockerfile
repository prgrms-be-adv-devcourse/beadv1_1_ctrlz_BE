# Build stage
FROM gradle:jdk21 AS build

WORKDIR /app

COPY gateway-service/build.gradle gateway-service/settings.gradle gateway-service/gradlew gateway-service/gradlew.bat ./gateway-service/
COPY gateway-service/gradle ./gateway-service/gradle
COPY gateway-service/gradlew gateway-service/gradlew.bat ./gateway-service/

COPY observability-config ./observability-config
COPY gateway-service/src ./gateway-service/src

WORKDIR /app/gateway-service
RUN sed -i 's/\r$//' ./gradlew
RUN ./gradlew clean build -x test --parallel --no-daemon --build-cache

FROM gcr.io/distroless/java21-debian12

WORKDIR /app

COPY --from=build /app/gateway-service/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Xms700m", "-Xmx700m", "-jar", "-Dspring.profiles.active=prod,secret", "app.jar"]
