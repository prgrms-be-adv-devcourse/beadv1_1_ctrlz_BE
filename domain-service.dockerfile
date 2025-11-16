FROM gradle:jdk21 AS build

WORKDIR /app

COPY domain-service/build.gradle domain-service/settings.gradle domain-service/gradlew domain-service/gradlew.bat ./domain-service/
COPY domain-service/gradle ./domain-service/gradle

COPY common ./common

COPY domain-service/src ./domain-service/src

WORKDIR /app/domain-service
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test --parallel --no-daemon

# 이미지 압축 라이브러리
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/domain-service/build/libs/*.jar app.jar

# 변경 예정
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=local", "app.jar"]
