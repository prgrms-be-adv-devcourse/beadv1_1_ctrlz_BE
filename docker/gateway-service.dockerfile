# syntax=docker/dockerfile:1.4
# BuildKit 고급 기능 활성화 (--link 등)

# Build stage
FROM gradle:jdk21 AS build
WORKDIR /app

# gradle wrapper 및 설정 파일들만 '먼저' 복사
COPY --link gateway-service/gradlew gateway-service/gradlew.bat ./gateway-service/
COPY --link gateway-service/gradle ./gateway-service/gradle
COPY --link gateway-service/settings.gradle ./gateway-service/

# 각 모듈의 build.gradle 파일 복사
COPY --link gateway-service/build.gradle ./gateway-service/
COPY --link observability-config/logging/build.gradle ./observability-config/logging/
COPY --link observability-config/zipkin/build.gradle ./observability-config/zipkin/

#의존성 다운로드
WORKDIR /app/gateway-service
RUN sed -i 's/\r$//' ./gradlew
RUN ./gradlew dependencies --no-daemon || true

# 나머지 소스 코드 복사
WORKDIR /app
COPY --link observability-config ./observability-config
COPY --link gateway-service/src ./gateway-service/src

#빌드
WORKDIR /app/gateway-service
RUN ./gradlew build -x test --parallel --no-daemon --build-cache

#jar 생성
# 이미지 압축 라이브러리 호환성을 위해 jammy 유지
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --link --from=build /app/gateway-service/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Xms512m", "-Xmx512m", "-jar", "-Dspring.profiles.active=prod,secret", "app.jar"]
