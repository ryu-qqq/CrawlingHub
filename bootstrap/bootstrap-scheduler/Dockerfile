# ========================================
# Crawlinghub Scheduler Dockerfile
# ========================================
# Based on Eclipse Temurin JRE 21 Alpine
# Metrics: ADOT Sidecar (Prometheus scrape)
# ========================================

FROM eclipse-temurin:21-jre-alpine

# Labels
LABEL maintainer="crawlinghub"
LABEL description="Crawlinghub Scheduler Service"

# Create non-root user
RUN addgroup -S app && adduser -S app -G app

# Set working directory
WORKDIR /app

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
HEALTHCHECK --interval=30s --timeout=10s --start-period=300s --retries=5 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# JVM options for containers (smaller heap for scheduler)
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -Djava.security.egd=file:/dev/./urandom"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
