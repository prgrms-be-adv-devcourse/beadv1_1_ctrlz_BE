FROM gradle:jdk21 AS build
WORKDIR /app

# gradle wrapper 및 설정 파일들만 '먼저' 복사
COPY account-service/gradlew account-service/gradlew.bat ./account-service/
COPY account-service/gradle ./account-service/gradle
COPY account-service/settings.gradle ./account-service/

# 각 모듈의 build.gradle 파일 복사
COPY account-service/build.gradle ./account-service/
COPY account-service/account-application/build.gradle .account-service/account-application
COPY account-service/user/build.gradle .account-service/user
COPY account-service/auth/build.gradle .account-service/auth
COPY common/build.gradle ./common/

#의존성 다운로드
WORKDIR /app/account-service
RUN sed -i 's/\r$//' ./gradlew # 개행(윈도우)
RUN ./gradlew dependencies --no-daemon || reutrn ture

# 나머지 소스 코드 복사
WORKDIR /app
COPY common ./common
COPY observability-config ./observability-config
COPY account-service/auth ./account-service/auth
COPY account-service/user ./account-service/user
COPY account-service/account-application ./account-service/account-application

#빌드
WORKDIR /app/account-service
RUN ./gradlew build -x test --parallel --no-daemon --build-cache



#jar 생성
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /app/account-service/account-application/build/libs/account-application-boot.jar app.jar

ENTRYPOINT ["java", "-Xms700m", "-Xmx700m", "-jar", "-Dspring.profiles.active=prod,secret", "app.jar"]