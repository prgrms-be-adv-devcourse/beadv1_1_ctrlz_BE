FROM gradle:jdk21 AS build

WORKDIR /app

COPY ../gateway-service/build.gradle gateway-service/settings.gradle gateway-service/gradlew gateway-service/gradlew.bat ./gateway-service/
COPY ../gateway-service/gradle ./gateway-service/gradle


COPY ../gateway-service/src ./gateway-service/src

WORKDIR /app/gateway-service
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test --parallel --no-daemon

FROM gcr.io/distroless/java21-debian12

WORKDIR /app

COPY --from=build /app/gateway-service/build/libs/*.jar app.jar

# 변경 예정
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=local", "app.jar"]
