FROM gradle:jdk21 AS build

WORKDIR /app

COPY payment-service/build.gradle payment-service/settings.gradle payment-service/gradlew payment-service/gradlew.bat ./payment-service/
COPY payment-service/gradle ./payment-service/gradle
COPY common/build.gradle ./common/build.gradle

COPY common ./common
COPY observability-config ./observability-config
COPY payment-service/src ./payment-service/src


WORKDIR /app/payment-service
RUN chmod +x ./gradlew
# 의존성 캐싱, gradle 변경 시만 작동
RUN ./gradlew dependencies --no-daemon
RUN ./gradlew clean build -x test --parallel --no-daemon


FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/payment-service/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Xms700m", "-Xmx700m", "-jar", "-Dspring.profiles.active=prod,secret", "app.jar"]



