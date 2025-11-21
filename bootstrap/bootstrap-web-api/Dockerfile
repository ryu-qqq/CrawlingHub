# ========================================
# Crawlinghub Web API Dockerfile
# ========================================
# Multi-stage build for optimized image
# Based on Eclipse Temurin JDK 21
# ========================================

FROM eclipse-temurin:21-jre-alpine

# Labels
LABEL maintainer="crawlinghub"
LABEL description="Crawlinghub Web API Service"

# Create non-root user
RUN addgroup -S app && adduser -S app -G app

# Set working directory
WORKDIR /app

# Copy JAR file
ARG JAR_FILE=bootstrap/bootstrap-web-api/build/libs/crawlinghub-web-api.jar
COPY ${JAR_FILE} app.jar

# Change ownership
RUN chown -R app:app /app

# Switch to non-root user
USER app

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM options for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -Djava.security.egd=file:/dev/./urandom"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
