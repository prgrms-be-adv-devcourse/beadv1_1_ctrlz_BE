# Build stage
FROM gradle:jdk21 AS build

WORKDIR /app


COPY account-service/build.gradle account-service/settings.gradle account-service/gradlew account-service/gradlew.bat ./account-service/
COPY account-service/gradle ./account-service/gradle
COPY account-service/gradlew account-service/gradlew.bat ./account-service/
COPY common/build.gradle ./common/

COPY common ./common
COPY observability-config ./observability-config
COPY account-service/auth ./account-service/auth
COPY account-service/user ./account-service/user
COPY account-service/account-application ./account-service/account-application

WORKDIR /app/account-service
RUN ./gradlew build -x test --parallel --no-daemon --build-cache

FROM openjdk:21-ea-21-slim

WORKDIR /app

COPY --from=build /app/account-service/account-application/build/libs/account-application-boot.jar app.jar

ENTRYPOINT ["java", "-Xms700m", "-Xmx700m", "-jar", "-Dspring.profiles.active=prod,secret", "app.jar"]
