# Research Findings: Web-Based Delivery Option

**Date**: 2025-11-05 | **Feature**: 003-web-delivery

## Executive Summary

Research completed for refactoring USV mission planner into web-based delivery option. Key decisions:
- Spring Boot with embedded React for single JAR deployment
- Stateless behavior pattern using immutable state objects
- Frontend-driven simulation via REST calls
- Heroku deployment with PORT binding

## 1. Spring Boot REST API with Embedded Frontend

### Decision: Single JAR with Embedded Static Resources
**Rationale**: Simplifies Heroku deployment, eliminates CORS complexity, unified port binding
**Alternatives considered**:
- Separate frontend/backend apps (rejected: complex coordination)
- Docker containers (rejected: Heroku buildpack simpler)

### Implementation Details

#### Maven Configuration
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
<plugin>
    <groupId>com.github.eirslett</groupId>
    <artifactId>frontend-maven-plugin</artifactId>
    <version>1.15.0</version>
    <configuration>
        <workingDirectory>frontend</workingDirectory>
    </configuration>
</plugin>
```

#### React Router Fallback Controller
```java
@Controller
public class ClientForwardController {
    @RequestMapping(value = "/{path:[^\.]*}")
    public String forward() {
        return "forward:/index.html";
    }
}
```

#### Heroku PORT Binding
```properties
server.port=${PORT:8080}
```

Procfile:
```
web: java -jar backend/target/usv-web.jar
```

## 2. Stateless Behavior Refactoring Pattern

### Decision: Direct Interface Evolution with State Objects
**Rationale**: No production code to maintain backward compatibility for - can directly evolve interface
**Alternatives considered**:
- Adapter pattern (rejected: unnecessary complexity since no backward compatibility needed)
- Keep stateful (rejected: not suitable for stateless REST)

### State Object Design
```java
public record BehaviourExecutionState(
    int currentWaypointIndex,
    BehaviourState state,
    double lastDistanceToWaypoint
) {
    public static BehaviourExecutionState initial() {
        return new BehaviourExecutionState(0, PENDING, Double.MAX_VALUE);
    }

    public BehaviourExecutionState advanceWaypoint() {
        return new BehaviourExecutionState(
            currentWaypointIndex + 1, state, Double.MAX_VALUE);
    }
}
```

### Refactoring Pattern
```java
// Evolve Behaviour interface directly (no backward compatibility needed)
public interface Behaviour {
    String getName();
    List<Waypoint> getWaypoints();

    // Stateless methods - accept and return state
    PlatformDemand calculateDemand(
        BehaviourExecutionState state,
        PlatformState platform);

    BehaviourExecutionState updateProgress(
        BehaviourExecutionState state,
        PlatformState platform);

    boolean isComplete(BehaviourExecutionState state);
}

// Implementation becomes stateless
public class ParallelTrackSearch implements Behaviour {
    // Configuration only (immutable)
    private final Polygon searchArea;
    private final List<Waypoint> waypoints;

    @Override
    public BehaviourExecutionState updateProgress(
        BehaviourExecutionState state,
        PlatformState platform) {
        // Returns new state, no mutations
    }
}
```

### Migration Strategy
1. Create state objects (BehaviourExecutionState)
2. Evolve Behaviour interface to accept/return state
3. Update all four behavior implementations to stateless
4. Update SimulationEngine to manage state externally
5. Update tests to work with new stateless interface
6. Validate desktop app still compiles and works

## 3. Frontend-Driven Simulation Architecture

### Decision: Frontend Controls Simulation Loop
**Rationale**: Backend remains stateless, survives dyno restarts, scales horizontally
**Alternatives considered**:
- WebSocket push from backend (rejected: stateful backend)
- Server-Sent Events (rejected: still requires backend state)

### Implementation Pattern
```typescript
// Frontend SimulationService
class SimulationService {
    private animationFrame: number;

    startSimulation() {
        const tick = async () => {
            const response = await fetch('/api/simulation/tick', {
                method: 'POST',
                body: JSON.stringify({
                    mission: this.mission,
                    platform: this.platformState,
                    deltaTime: this.deltaTime
                })
            });

            this.platformState = await response.json();
            this.render(this.platformState);

            if (this.running) {
                this.animationFrame = requestAnimationFrame(tick);
            }
        };
        tick();
    }
}
```

### REST Endpoint Design
```java
@PostMapping("/api/simulation/tick")
public SimulationTickResponse tick(@RequestBody SimulationTickRequest request) {
    // Stateless computation
    BehaviourExecutionState behaviorState = request.getBehaviorState();
    PlatformState platform = request.getPlatformState();

    // Calculate next state
    PlatformDemand demand = behavior.calculateDemand(behaviorState, platform);
    PlatformState nextPlatform = dynamics.update(platform, demand, request.getDeltaTime());
    BehaviourExecutionState nextBehavior = behavior.updateProgress(behaviorState, platform);

    return new SimulationTickResponse(nextPlatform, nextBehavior);
}
```

## 4. TypeScript Type Definitions

### Decision: Generate from Java DTOs
**Rationale**: Single source of truth, prevents drift
**Alternatives considered**:
- Manual maintenance (rejected: error-prone)
- GraphQL schema (rejected: overkill for REST)

### Type Structure
```typescript
// Platform state
interface PlatformState {
    position: Position;
    heading: number;
    speed: number;
    depth: number;
    timestamp: number;
}

// Behavior execution state
interface BehaviorExecutionState {
    currentWaypointIndex: number;
    state: 'PENDING' | 'EXECUTING' | 'COMPLETE';
    lastDistanceToWaypoint: number;
}

// Simulation tick request
interface SimulationTickRequest {
    mission: Mission;
    platformState: PlatformState;
    behaviorState: BehaviorExecutionState;
    deltaTime: number;
}

// Simulation tick response
interface SimulationTickResponse {
    platformState: PlatformState;
    behaviorState: BehaviorExecutionState;
    demand: PlatformDemand;
}
```

## 5. File Management Strategy

### Decision: Client-Side Only
**Rationale**: Heroku ephemeral filesystem, no persistent storage needed
**Alternatives considered**:
- S3 integration (rejected: unnecessary complexity)
- PostgreSQL (rejected: overkill for file storage)

### Implementation
```typescript
// Upload
const handleFileUpload = (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    const reader = new FileReader();
    reader.onload = (e) => {
        const mission = JSON.parse(e.target?.result as string);
        loadMission(mission);
    };
    reader.readAsText(file);
};

// Download
const handleDownload = () => {
    const json = JSON.stringify(mission, null, 2);
    const blob = new Blob([json], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'mission.json';
    a.click();
};
```

## 6. Constitution Updates Required

### Principle IV: Behavior-Driven Design
**Current**: "Each behavior is self-contained with its own waypoint generation and completion logic"
**Update needed**: Add "Behaviors may be implemented as stateless pure functions with external state management"

**Rationale**: Stateless pattern still maintains behavior interface contract but externalizes state for REST compatibility.

## 7. Performance Considerations

### Target Metrics (from spec)
- <50ms POST /simulation/tick latency
- 60 FPS map rendering
- 20+ FPS simulation

### Optimization Strategies
1. **Request batching**: Frontend can batch multiple ticks if latency high
2. **State compression**: Only send deltas instead of full state
3. **Caching**: Browser caches static resources (React build)
4. **CDN**: Leaflet tiles from CDN reduces server load

## 8. Testing Strategy

### Phase 1 Validation (Desktop)
- Run existing TestFX suite after behavior refactoring
- Add unit tests for stateless logic classes
- Verify identical behavior with state externalization

### Backend Testing
```java
@Test
void testSimulationTick() {
    SimulationTickRequest request = new SimulationTickRequest(
        mission, platformState, behaviorState, 0.1);

    SimulationTickResponse response = controller.tick(request);

    assertNotNull(response.getPlatformState());
    assertEquals(EXECUTING, response.getBehaviorState().getState());
}
```

### Frontend Testing
```typescript
test('simulation tick updates platform state', async () => {
    const service = new SimulationService();
    const initial = { position: { lat: 50.6, lon: -2.4 }, heading: 90, speed: 5 };

    const next = await service.tick(initial, 0.1);

    expect(next.position).not.toEqual(initial.position);
});
```

## 9. Deployment Checklist

1. ✅ Spring Boot configured for embedded static resources
2. ✅ Heroku PORT binding via environment variable
3. ✅ Maven builds React and embeds in JAR
4. ✅ Procfile for single-process deployment
5. ✅ Stateless backend architecture
6. ✅ Frontend-driven simulation loop
7. ✅ Client-side file management
8. ✅ TypeScript type definitions documented

## 10. Risk Mitigation

### Risk: REST Latency Impact on Simulation
**Mitigation**:
- Target <50ms latency (adequate for 20 FPS)
- Frontend interpolation between ticks if needed
- Batch tick requests if latency spikes

### Risk: Behavior Refactoring Breaks Desktop
**Mitigation**:
- Adapter pattern preserves interface
- Phase 1 includes full test validation
- Incremental refactoring (one behavior at a time)

### Risk: Type Definition Drift
**Mitigation**:
- Phase 2 checkpoint for type review
- Generate from Java DTOs where possible
- Integration tests validate contract

## Conclusion

All technical unknowns resolved. Ready for Phase 1 design and contract generation. Key innovation is stateless behavior pattern enabling horizontal scaling while maintaining desktop compatibility through adapter pattern.