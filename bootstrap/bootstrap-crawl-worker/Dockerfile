# ========================================
# Crawlinghub Crawl Worker Dockerfile
# ========================================
# Based on Eclipse Temurin JRE 21 Alpine
# Metrics: ADOT Sidecar (Prometheus scrape)
# ========================================

FROM eclipse-temurin:21-jre-alpine

# Labels
LABEL maintainer="crawlinghub"
LABEL description="Crawlinghub Crawl Worker Service"

# Create non-root user
RUN addgroup -S app && adduser -S app -G app

# Set working directory
WORKDIR /app

# Copy JAR file
ARG JAR_FILE=bootstrap/bootstrap-crawl-worker/build/libs/crawlinghub-crawl-worker.jar
COPY ${JAR_FILE} app.jar

# Change ownership
RUN chown -R app:app /app

# Switch to non-root user
USER app

# Expose actuator port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=300s --retries=5 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# JVM options for containers (optimized for crawl worker)
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -Djava.security.egd=file:/dev/./urandom"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
