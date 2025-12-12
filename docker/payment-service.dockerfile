# Build stage
FROM gradle:jdk21 AS build
WORKDIR /app

# gradle wrapper 및 설정 파일들만 '먼저' 복사
COPY payment-service/gradlew payment-service/gradlew.bat ./payment-service/
COPY payment-service/gradle ./payment-service/gradle
COPY payment-service/settings.gradle ./payment-service/

# 각 모듈의 build.gradle 파일 복사
COPY payment-service/build.gradle ./payment-service/
COPY common/build.gradle ./common/
COPY observability-config/logging/build.gradle ./observability-config/logging/
COPY observability-config/zipkin/build.gradle ./observability-config/zipkin/

#의존성 다운로드
WORKDIR /app/payment-service
RUN sed -i 's/\r$//' ./gradlew
RUN ./gradlew dependencies --no-daemon

# 나머지 소스 코드 복사
WORKDIR /app
COPY common ./common
COPY observability-config ./observability-config
COPY payment-service/src ./payment-service/src

#빌드
WORKDIR /app/payment-service
RUN sed -i 's/\r$//' ./gradlew
RUN ./gradlew build -x test --parallel --no-daemon --build-cache

#jar 생성
# 이미지 압축 라이브러리 호환성을 위해 jammy 유지
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/payment-service/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Xms700m", "-Xmx700m", "-jar", "-Dspring.profiles.active=prod,secret", "app.jar"]



