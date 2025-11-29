# ========================================
# Crawlinghub Scheduler Dockerfile
# ========================================
# Multi-stage build for optimized image
# Based on Eclipse Temurin JDK 21
# With AWS ADOT Agent for OpenTelemetry
# ========================================

FROM eclipse-temurin:21-jre-alpine

# Labels
LABEL maintainer="crawlinghub"
LABEL description="Crawlinghub Scheduler Service"

# Create non-root user
RUN addgroup -S app && adduser -S app -G app

# Set working directory
WORKDIR /app

# Download AWS ADOT Agent for Java instrumentation
ADD https://github.com/aws-observability/aws-otel-java-instrumentation/releases/latest/download/aws-opentelemetry-agent.jar /app/aws-opentelemetry-agent.jar

# Copy JAR file
ARG JAR_FILE=bootstrap/bootstrap-scheduler/build/libs/crawlinghub-scheduler.jar
COPY ${JAR_FILE} app.jar

# Change ownership
RUN chown -R app:app /app

# Switch to non-root user
USER app

# Expose actuator port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# JVM options for containers (smaller heap for scheduler)
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -Djava.security.egd=file:/dev/./urandom"

# ADOT Agent options
ENV OTEL_AGENT_OPTS="-javaagent:/app/aws-opentelemetry-agent.jar"

# OpenTelemetry configuration
ENV OTEL_SERVICE_NAME="crawlinghub-scheduler"
ENV OTEL_EXPORTER_OTLP_PROTOCOL="grpc"
ENV OTEL_EXPORTER_OTLP_ENDPOINT="http://localhost:4317"
ENV OTEL_METRICS_EXPORTER="otlp"
ENV OTEL_TRACES_EXPORTER="otlp"
ENV OTEL_LOGS_EXPORTER="none"
ENV OTEL_PROPAGATORS="xray,tracecontext,baggage"
ENV OTEL_RESOURCE_ATTRIBUTES="service.namespace=crawlinghub,deployment.environment=prod"

# Run application with ADOT Agent
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS $OTEL_AGENT_OPTS -jar app.jar"]
