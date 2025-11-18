# Build stage
FROM gradle:jdk21 AS build

WORKDIR /app


COPY account-service/build.gradle account-service/settings.gradle account-service/gradlew account-service/gradlew.bat ./account-service/
COPY account-service/gradle ./account-service/gradle
COPY common/build.gradle ./common/build.gradle

COPY common ./common
COPY account-service/auth ./account-service/auth
COPY account-service/user ./account-service/user
COPY account-service/account-application ./account-service/account-application

WORKDIR /app/account-service
RUN chmod +x ./gradlew
# 의존성 캐싱, gradle 변경 시만 작동
RUN ./gradlew dependencies --no-daemon
RUN ./gradlew clean build -x test --parallel --no-daemon

FROM gcr.io/distroless/java21-debian12

WORKDIR /app

COPY --from=build /app/account-service/account-application/build/libs/account-application-boot.jar app.jar

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod,secret" ,"app.jar"]
