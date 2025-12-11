# Build stage
FROM gradle:jdk21 AS build

WORKDIR /app

COPY settlement-service/build.gradle settlement-service/settings.gradle settlement-service/gradlew settlement-service/gradlew.bat ./settlement-service/
COPY settlement-service/gradle ./settlement-service/gradle
COPY settlement-service/gradlew settlement-service/gradlew.bat ./settlement-service/
COPY common/build.gradle ./common/

WORKDIR /app/settlement-service
RUN chmod +x ./gradlew && \
    ./gradlew dependencies --no-daemon --build-cache

WORKDIR /app
COPY common ./common
COPY observability-config ./observability-config
COPY settlement-service/src ./settlement-service/src

WORKDIR /app/settlement-service
RUN ./gradlew build -x test --parallel --no-daemon --build-cache

FROM gcr.io/distroless/java21-debian12

WORKDIR /app

COPY --from=build /app/settlement-service/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Xms700m", "-Xmx700m", "-jar", "-Dspring.profiles.active=prod,secret", "app.jar"]
