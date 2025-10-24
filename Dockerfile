# Multi-stage Dockerfile for JavaFX application with JPro
# Stage 1: Build the application with Maven, then package with Gradle+JPro
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application with Maven (creates target/*.jar)
RUN mvn clean package -DskipTests -B

# Install Gradle for JPro packaging
RUN apk add --no-cache wget unzip && \
    wget https://services.gradle.org/distributions/gradle-8.5-bin.zip && \
    unzip gradle-8.5-bin.zip -d /opt && \
    rm gradle-8.5-bin.zip && \
    ln -s /opt/gradle-8.5/bin/gradle /usr/bin/gradle

# Copy Gradle build files
COPY build.gradle settings.gradle ./

# Download Gradle dependencies
RUN gradle dependencies --no-daemon

# Build JPro release bundle
RUN gradle jproRelease --no-daemon

# Stage 2: Runtime image
FROM eclipse-temurin:21-jre-alpine

# Install necessary runtime dependencies
RUN apk add --no-cache \
    fontconfig \
    ttf-dejavu \
    bash \
    curl

# Set working directory
WORKDIR /app

# Copy built JPro release from builder stage
COPY --from=builder /app/build/jpro/release /app/jpro

# Expose JPro default port
EXPOSE 8080

# Set environment variables (can be overridden at runtime)
ENV JPRO_PORT=8080
ENV JAVA_OPTS="-Xmx512m"

# Health check endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/ || exit 1

# Create entrypoint script
RUN echo '#!/bin/bash' > /app/entrypoint.sh && \
    echo 'set -e' >> /app/entrypoint.sh && \
    echo '' >> /app/entrypoint.sh && \
    echo 'echo "Starting JPro server on port $JPRO_PORT..."' >> /app/entrypoint.sh && \
    echo 'cd /app/jpro' >> /app/entrypoint.sh && \
    echo 'exec java $JAVA_OPTS -jar jpro-server.jar' >> /app/entrypoint.sh && \
    chmod +x /app/entrypoint.sh

# Run the entrypoint script
CMD ["/app/entrypoint.sh"]
