# syntax=docker/dockerfile:1.4
# BuildKit 고급 기능 활성화 (--link 등)

FROM gradle:jdk21 AS build
WORKDIR /app

# gradle wrapper 및 설정 파일들만 '먼저' 복사
COPY --link account-service/gradlew account-service/gradlew.bat ./account-service/
COPY --link account-service/gradle ./account-service/gradle
COPY --link account-service/settings.gradle ./account-service/

# 각 모듈의 build.gradle 파일 복사
COPY --link account-service/build.gradle ./account-service/
COPY --link account-service/account-application/build.gradle .account-service/account-application
COPY --link account-service/user/build.gradle .account-service/user
COPY --link account-service/auth/build.gradle .account-service/auth
COPY --link common/build.gradle ./common/

#의존성 다운로드
WORKDIR /app/account-service
RUN sed -i 's/\r$//' ./gradlew # 개행(윈도우)
RUN ./gradlew dependencies --no-daemon || reutrn ture

# 나머지 소스 코드 복사
WORKDIR /app
COPY --link common ./common
COPY --link observability-config ./observability-config
COPY --link account-service/auth ./account-service/auth
COPY --link account-service/user ./account-service/user
COPY --link account-service/account-application ./account-service/account-application

#빌드
WORKDIR /app/account-service
RUN ./gradlew build -x test --parallel --no-daemon --build-cache

#jar 생성
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --link --from=build /app/account-service/account-application/build/libs/account-application-boot.jar app.jar

ENTRYPOINT ["java", "-Xms512m", "-Xmx512m", "-jar", "-Dspring.profiles.active=prod,secret", "app.jar"]