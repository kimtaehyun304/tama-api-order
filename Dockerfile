# 1️⃣ 빌드 단계
FROM gradle:8-jdk17 AS builder
WORKDIR /build

COPY . .

RUN --mount=type=secret,id=gradle_properties,target=/home/gradle/.gradle/gradle.properties \
    gradle clean build -x test


# 2️⃣ 실행 단계
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /build/build/libs/*.jar app.jar

EXPOSE 5002

ENTRYPOINT ["java", "-jar", "app.jar"]
