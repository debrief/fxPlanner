# Quickstart Guide: Web-Based Delivery Option

**Feature**: 003-web-delivery | **Date**: 2025-11-05

## Overview

This guide covers the three-phase implementation of the web-based delivery option for the USV Mission Planner. The web version runs alongside the existing JavaFX desktop application, providing browser-based access via Heroku deployment.

## Prerequisites

- Java 25 (existing codebase)
- Node.js 20+ and npm 10+
- Maven 3.9+
- Git
- Heroku CLI (for deployment)
- Modern web browser

## Phase 1: Behavior Refactoring (Desktop First)

**Goal**: Refactor behaviors to stateless pure functions while maintaining desktop compatibility.

### Step 1: Create State Objects

```bash
cd src/main/java/com/planetmayo/usvsim/model/behaviour
```

Create `BehaviourExecutionState.java`:
```java
public record BehaviourExecutionState(
    int currentWaypointIndex,
    BehaviourState state,
    double lastDistanceToWaypoint
) {
    public static BehaviourExecutionState initial() {
        return new BehaviourExecutionState(0, BehaviourState.PENDING, Double.MAX_VALUE);
    }
}
```

### Step 2: Evolve Behaviour Interface

Update `Behaviour.java`:
```java
public interface Behaviour {
    // Stateless methods - accept and return state
    PlatformDemand calculateDemand(
        BehaviourExecutionState state,
        PlatformState platform);

    BehaviourExecutionState updateProgress(
        BehaviourExecutionState state,
        PlatformState platform);

    boolean isComplete(BehaviourExecutionState state);
}
```

### Step 3: Refactor One Behavior (Start with WaypointTransit)

1. Update `WaypointTransit.java` to implement stateless interface
2. Remove instance variables holding execution state
3. Update tests: `mvn test -Dtest="WaypointTransitTest"`

### Step 3: Validate Desktop Application

```bash
# Run full test suite
mvn clean test

# Run desktop application
mvn javafx:run

# Run E2E tests
mvn test -Dtest="*E2ETest"
```

**GATE**: Desktop must work identically before proceeding to Phase 2.

## Phase 2: Backend REST API

**Goal**: Create Spring Boot REST API with stateless simulation endpoint.

### Step 1: Setup Spring Boot Project

```bash
# Create backend directory
mkdir -p backend/src/main/java/com/planetmayo/usvsim/api
cd backend

# Copy refactored models
cp -r ../src/main/java/com/planetmayo/usvsim/model ./src/main/java/com/planetmayo/usvsim/
```

### Step 2: Add Spring Boot Dependencies

Create `backend/pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

### Step 3: Create REST Controllers

```java
@RestController
@RequestMapping("/api")
public class SimulationController {
    @PostMapping("/simulation/tick")
    public SimulationTickResponse tick(@RequestBody SimulationTickRequest request) {
        // Stateless computation
    }
}
```

### Step 4: Test Backend

```bash
cd backend
mvn spring-boot:run

# Test endpoint
curl -X POST http://localhost:8080/api/simulation/tick \
  -H "Content-Type: application/json" \
  -d '{"mission": {...}, "platformState": {...}, "deltaTime": 0.1}'
```

### Step 5: Document TypeScript Types

**CHECKPOINT**: Review `contracts/types.ts` before Phase 3.

## Phase 3: React Frontend

**Goal**: Build React frontend that drives simulation via REST calls.

### Step 1: Setup React Project

```bash
cd frontend
npx create-react-app . --template typescript
npm install react@19 react-dom@19 leaflet react-leaflet axios
```

### Step 2: Configure Proxy for Development

Edit `frontend/package.json`:
```json
{
  "proxy": "http://localhost:8080"
}
```

### Step 3: Implement Core Components

```bash
# Create component structure
mkdir -p src/components/behaviors
mkdir -p src/services
mkdir -p src/types

# Copy TypeScript types
cp ../specs/003-web-delivery/contracts/types.ts src/types/
```

### Step 4: Build Frontend

```bash
npm run build

# Copy build to backend
cp -r build/* ../backend/src/main/resources/static/
```

## Deployment

### Local Testing

```bash
# Build complete application
cd backend
mvn clean package

# Run locally
PORT=5000 java -jar target/usv-web.jar

# Test
open http://localhost:5000
```

### Heroku Deployment

```bash
# Create Heroku app
heroku create usv-mission-planner

# Create Procfile in root
echo "web: java -jar backend/target/usv-web.jar" > Procfile

# Deploy
git add .
git commit -m "Web delivery implementation"
git push heroku 003-web-delivery:main

# Open app
heroku open
```

## Development Workflow

### Backend Development

```bash
cd backend
mvn spring-boot:run
# API available at http://localhost:8080/api
```

### Frontend Development

```bash
cd frontend
npm start
# React dev server at http://localhost:3000
# Proxies API calls to :8080
```

### Full Build

```bash
# From root
mvn clean package
# Produces single JAR with embedded frontend
```

## Testing

### Unit Tests

```bash
# Backend
cd backend && mvn test

# Frontend
cd frontend && npm test
```

### Integration Tests

```bash
# Test REST endpoints
mvn test -Dtest="*ControllerTest"

# Test frontend-backend integration
npm run test:integration
```

### Cross-Version Compatibility

1. Save mission from desktop version
2. Load in web version
3. Verify identical behavior
4. Save from web, load in desktop

## Common Issues

### Issue: Behavior refactoring breaks desktop

**Solution**: Use adapter pattern to preserve interface:
```java
public class ParallelTrackSearch implements Behaviour {
    private final ParallelTrackSearchLogic logic;
    private BehaviourExecutionState state;

    @Override
    public void updateProgress(PlatformState platform) {
        state = logic.updateProgress(state, platform);
    }
}
```

### Issue: CORS errors in development

**Solution**: Use React proxy in development, no CORS in production (same origin).

### Issue: Heroku build fails

**Solution**: Add Maven memory settings:
```properties
# system.properties
java.runtime.version=21
MAVEN_OPTS=-Xmx1024m
```

### Issue: Simulation feels laggy

**Solution**: Ensure <50ms tick latency:
- Optimize JSON serialization
- Use requestAnimationFrame
- Consider state compression

## Performance Monitoring

### Backend Metrics

```java
@GetMapping("/api/metrics")
public Metrics getMetrics() {
    return new Metrics(
        averageTickLatency,
        ticksPerSecond,
        activeSessions
    );
}
```

### Frontend Metrics

```typescript
const measureTickLatency = () => {
    const start = performance.now();
    await simulationService.tick(state);
    const latency = performance.now() - start;
    console.log(`Tick latency: ${latency}ms`);
};
```

## Next Steps

1. **Constitution Update**: Update Principle IV to reflect stateless behaviors
2. **Performance Tuning**: Optimize for <50ms tick latency
3. **UI Polish**: ASCII mockups for React components
4. **Documentation**: Update README with web deployment option

## Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/)
- [React Documentation](https://react.dev/)
- [Leaflet Documentation](https://leafletjs.com/)
- [Heroku Java Support](https://devcenter.heroku.com/articles/java-support)