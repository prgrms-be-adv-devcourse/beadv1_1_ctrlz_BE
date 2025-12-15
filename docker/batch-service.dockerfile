# Build stage
FROM gradle:jdk21 AS build
WORKDIR /app

# gradle wrapper 및 설정 파일들만 '먼저' 복사
COPY batch-service/gradlew batch-service/gradlew.bat ./batch-service/
COPY batch-service/gradle ./batch-service/gradle
COPY batch-service/settings.gradle ./batch-service/

# 각 모듈의 build.gradle 파일 복사
COPY batch-service/build.gradle ./batch-service/
COPY batch-service/settlement/build.gradle ./batch-service/settlement/
COPY batch-service/search/build.gradle ./batch-service/search/
COPY common/build.gradle ./common/
COPY observability-config/logging/build.gradle ./observability-config/logging/
COPY observability-config/zipkin/build.gradle ./observability-config/zipkin/

#의존성 다운로드
WORKDIR /app/batch-service
RUN sed -i 's/\r$//' ./gradlew
RUN ./gradlew dependencies --no-daemon || true

# 나머지 소스 코드 복사
WORKDIR /app
COPY common ./common
COPY observability-config ./observability-config
COPY batch-service/src ./batch-service/src
COPY batch-service/settlement/src ./batch-service/settlement/src
COPY batch-service/search/src ./batch-service/search/src

#빌드
WORKDIR /app/batch-service
RUN ./gradlew build -x test --parallel --no-daemon --build-cache

#jar 생성
# 이미지 압축 라이브러리 호환성을 위해 jammy 유지
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/batch-service/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Xms700m", "-Xmx700m", "-jar", "-Dspring.profiles.active=prod,secret", "app.jar"]
