# Build stage
FROM gradle:jdk21 AS build

WORKDIR /app

COPY domain-service/build.gradle domain-service/settings.gradle domain-service/gradlew domain-service/gradlew.bat ./domain-service/
COPY domain-service/gradle ./domain-service/gradle
COPY domain-service/gradlew domain-service/gradlew.bat ./domain-service/
COPY common/build.gradle ./common/

COPY common ./common
COPY observability-config ./observability-config
COPY domain-service/src ./domain-service/src


WORKDIR /app/domain-service
RUN sed -i 's/\r$//' ./gradlew
RUN ./gradlew clean build -x test --parallel --no-daemon --build-cache

# 이미지 압축 라이브러리
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/domain-service/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Xms700m", "-Xmx700m", "-jar", "-Dspring.profiles.active=prod,secret", "app.jar"]



