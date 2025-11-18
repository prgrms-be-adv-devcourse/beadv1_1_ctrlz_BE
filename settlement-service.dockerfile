FROM gradle:jdk21 AS build

WORKDIR /app

COPY settlement-service/build.gradle settlement-service/settings.gradle settlement-service/gradlew settlement-service/gradlew.bat ./settlement-service/
COPY settlement-service/gradle ./settlement-service/gradle
COPY settlement-service/src ./settlement-service/src
COPY common ./common

WORKDIR /app/settlement-service
RUN ./gradlew dependencies --no-daemon
RUN ./gradlew clean build -x test --parallel --no-daemon

FROM gcr.io/distroless/java21-debian12

WORKDIR /app

COPY --from=build /app/settlement-service/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod" ,"app.jar"]
