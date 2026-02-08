# Multi-stage build: 빌드 단계와 실행 단계 분리
FROM gradle:8.5-jdk21 AS build
WORKDIR /app

# Gradle 캐시 활용: 의존성만 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

RUN gradle dependencies --no-daemon || true

# 소스 복사 후 JAR 빌드
COPY . .
RUN gradle clean bootJar --no-daemon

# 실행 단계 (경량 이미지)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 타임존 + 폰트 (엑셀/PDF 생성 시 libfreetype 필요)
RUN apk add --no-cache tzdata fontconfig font-dejavu freetype wget && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    fc-cache -fv && \
    apk del tzdata

# 빌드된 JAR 복사 (컨테이너 내 경로: /app/app.jar)
COPY --from=build /app/build/libs/*.jar /app/app.jar

RUN mkdir -p /app/logs

# Railway는 PORT 환경변수로 포트 지정
ENV PORT=8082
EXPOSE 8082

ENV SPRING_PROFILES_ACTIVE=prod

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider "http://localhost:${PORT}/login" || exit 1

# 주의: Railway에서 "Start Command"를 비워 두어야 이 ENTRYPOINT가 사용됩니다.
# build/libs/INET-0.0.1-SNAPSHOT.jar 는 이 이미지에 없음 (JAR는 /app/app.jar 로만 존재)
ENTRYPOINT ["sh", "-c", "exec java \
  -Djava.security.egd=file:/dev/./urandom \
  -Dfile.encoding=UTF-8 \
  -Xmx512m \
  -Xms256m \
  -jar /app/app.jar"]
