# Build stage
FROM gradle:jdk21 AS build

WORKDIR /app

COPY payment-service/build.gradle payment-service/settings.gradle payment-service/gradlew payment-service/gradlew.bat ./payment-service/
COPY payment-service/gradle ./payment-service/gradle
COPY payment-service/gradlew payment-service/gradlew.bat ./payment-service/
COPY common/build.gradle ./common/

WORKDIR /app/payment-service
RUN chmod +x ./gradlew && \
    ./gradlew dependencies --no-daemon --build-cache

WORKDIR /app
COPY common ./common
COPY observability-config ./observability-config
COPY payment-service/src ./payment-service/src


WORKDIR /app/payment-service
RUN sed -i 's/\r$//' ./gradlew
RUN ./gradlew build -x test --parallel --no-daemon --build-cache


FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/payment-service/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Xms700m", "-Xmx700m", "-jar", "-Dspring.profiles.active=prod,secret", "app.jar"]



