# Multi-stage Dockerfile for JavaFX application with JPro
# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application with JPro
RUN mvn clean package -DskipTests -B

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

# Copy built artifacts from builder stage
COPY --from=builder /app/target /app/target
COPY --from=builder /app/pom.xml /app/pom.xml

# Install Maven (needed for JPro execution)
RUN apk add --no-cache maven

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
    echo '# If GITHUB_REPO and PR_BRANCH are provided, clone and build' >> /app/entrypoint.sh && \
    echo 'if [ -n "$GITHUB_REPO" ] && [ -n "$PR_BRANCH" ]; then' >> /app/entrypoint.sh && \
    echo '    echo "Cloning repository: $GITHUB_REPO"' >> /app/entrypoint.sh && \
    echo '    cd /tmp' >> /app/entrypoint.sh && \
    echo '    git clone $GITHUB_REPO repo' >> /app/entrypoint.sh && \
    echo '    cd repo' >> /app/entrypoint.sh && \
    echo '    echo "Checking out branch: $PR_BRANCH"' >> /app/entrypoint.sh && \
    echo '    git checkout $PR_BRANCH' >> /app/entrypoint.sh && \
    echo '    echo "Building application..."' >> /app/entrypoint.sh && \
    echo '    mvn clean package -DskipTests -B' >> /app/entrypoint.sh && \
    echo '    cd /tmp/repo' >> /app/entrypoint.sh && \
    echo 'fi' >> /app/entrypoint.sh && \
    echo '' >> /app/entrypoint.sh && \
    echo 'echo "Starting JPro server on port $JPRO_PORT..."' >> /app/entrypoint.sh && \
    echo 'cd /app' >> /app/entrypoint.sh && \
    echo 'exec mvn jpro:run' >> /app/entrypoint.sh && \
    chmod +x /app/entrypoint.sh

# Run the entrypoint script
CMD ["/app/entrypoint.sh"]
