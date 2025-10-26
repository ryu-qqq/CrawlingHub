# ============================================================================
# Multi-Stage Dockerfile for CrawlingHub API Server
# ============================================================================
# Stage 1: Build
# Stage 2: Runtime
# ============================================================================

# ============================================================================
# Stage 1: Build Stage
# ============================================================================
# Platform 명시: ECS Fargate는 linux/amd64 (x86_64)만 지원
FROM --platform=linux/amd64 gradle:8.5-jdk21-alpine AS builder

LABEL maintainer="CrawlingHub Platform Team"
LABEL stage="builder"

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 캐시 최적화를 위한 의존성 파일 먼저 복사
COPY gradle/ gradle/
COPY gradlew .
COPY gradle.properties .
COPY settings.gradle.kts .
COPY build.gradle.kts .

# 모듈별 build.gradle.kts 파일 복사
COPY domain/build.gradle.kts domain/
COPY application/build.gradle.kts application/
COPY adapter/adapter-in-admin-web/build.gradle.kts adapter/adapter-in-admin-web/
COPY adapter/adapter-in-event/build.gradle.kts adapter/adapter-in-event/
COPY adapter/adapter-out-persistence-jpa/build.gradle.kts adapter/adapter-out-persistence-jpa/
COPY adapter/adapter-out-aws-s3/build.gradle.kts adapter/adapter-out-aws-s3/
COPY adapter/adapter-out-aws-sqs/build.gradle.kts adapter/adapter-out-aws-sqs/
COPY adapter/adapter-out-aws-eventbridge/build.gradle.kts adapter/adapter-out-aws-eventbridge/
COPY adapter/adapter-out-redis/build.gradle.kts adapter/adapter-out-redis/
COPY adapter/adapter-out-mustit-api/build.gradle.kts adapter/adapter-out-mustit-api/
COPY bootstrap/bootstrap-web-api/build.gradle.kts bootstrap/bootstrap-web-api/

# 의존성 다운로드 (캐시 레이어)
RUN gradle dependencies --no-daemon || true

# 전체 소스 코드 복사
COPY . .

# Gradle 빌드 실행 (테스트 제외)
RUN gradle :bootstrap:bootstrap-web-api:bootJar --no-daemon -x test

# JAR 파일 위치 확인 및 이름 변경
RUN mv bootstrap/bootstrap-web-api/build/libs/*.jar app.jar

# ============================================================================
# Stage 2: Runtime Stage
# ============================================================================
FROM --platform=linux/amd64 eclipse-temurin:21-jre-alpine

LABEL maintainer="CrawlingHub Platform Team"
LABEL service="api-server"
LABEL version="1.0.0"

# 보안 및 운영을 위한 사용자 생성
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# 작업 디렉토리 설정
WORKDIR /app

# 타임존 설정 (Asia/Seoul)
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apk del tzdata

# 헬스체크를 위한 curl 설치
RUN apk add --no-cache curl

# Builder 스테이지에서 JAR 파일 복사
COPY --from=builder /app/app.jar app.jar

# 소유권 변경
RUN chown -R appuser:appgroup /app

# 애플리케이션 사용자로 전환
USER appuser

# 포트 노출
EXPOSE 8080

# JVM 메모리 설정 (환경변수로 오버라이드 가능)
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# 헬스체크 설정 (Spring Boot Actuator)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
