# Build stage
FROM gradle:jdk21 AS build
WORKDIR /app

# gradle wrapper 및 설정 파일들만 '먼저' 복사
COPY ai-service/gradlew ai-service/gradlew.bat ./ai-service/
COPY ai-service/gradle ./ai-service/gradle
COPY ai-service/settings.gradle ./ai-service/

# 각 모듈의 build.gradle 파일 복사
COPY ai-service/build.gradle ./ai-service/
COPY observability-config/logging/build.gradle ./observability-config/logging/
COPY observability-config/zipkin/build.gradle ./observability-config/zipkin/

#의존성 다운로드
WORKDIR /app/ai-service
RUN sed -i 's/\r$//' ./gradlew
RUN ./gradlew dependencies --no-daemon || true

# 나머지 소스 코드 복사
WORKDIR /app
COPY observability-config ./observability-config
COPY ai-service/src ./ai-service/src

#빌드
WORKDIR /app/ai-service
RUN ./gradlew build -x test --parallel --no-daemon --build-cache

#jar 생성
# 이미지 압축 라이브러리 호환성을 위해 jammy 유지
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/ai-service/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Xms512m", "-Xmx512m", "-jar", "-Dspring.profiles.active=prod,secret", "app.jar"]
