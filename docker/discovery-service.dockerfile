# Build stage
FROM gradle:jdk21 AS build
WORKDIR /app

# gradle wrapper 및 설정 파일들만 '먼저' 복사
COPY discovery-service/gradlew discovery-service/gradlew.bat ./discovery-service/
COPY discovery-service/gradle ./discovery-service/gradle
COPY discovery-service/settings.gradle ./discovery-service/

# 각 모듈의 build.gradle 파일 복사
COPY discovery-service/build.gradle ./discovery-service/
COPY observability-config/logging/build.gradle ./observability-config/logging/

#의존성 다운로드
WORKDIR /app/discovery-service
RUN sed -i 's/\r$//' ./gradlew
RUN ./gradlew dependencies --no-daemon || true

# 나머지 소스 코드 복사
WORKDIR /app
COPY observability-config ./observability-config
COPY discovery-service/src ./discovery-service/src

#빌드
WORKDIR /app/discovery-service
RUN ./gradlew clean build -x test --parallel --no-daemon --build-cache

#jar 생성
# 이미지 압축 라이브러리 호환성을 위해 jammy 유지
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/discovery-service/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Xms700m", "-Xmx700m", "-jar", "-Dspring.profiles.active=prod,secret", "app.jar"]
