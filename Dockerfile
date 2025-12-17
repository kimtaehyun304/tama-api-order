# 베이스 이미지
FROM openjdk:17-slim

WORKDIR /app

# 필요한 패키지 설치 (wget, tar, gzip, procps)
RUN apt-get update && \
    apt-get install -y wget tar gzip procps curl && \
    rm -rf /var/lib/apt/lists/*

# 애플리케이션 JAR 복사
COPY application.jar ./

EXPOSE 5000

CMD ["java", "-jar", "application.jar"]