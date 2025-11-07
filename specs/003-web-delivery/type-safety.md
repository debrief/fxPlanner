# Type Safety Strategy: Web-Based Delivery Option

**Feature**: 003-web-delivery | **Date**: 2025-11-05

## Problem

TypeScript (frontend) and Java (backend) need to exchange data via REST API. Type definitions must stay synchronized to prevent runtime errors from type mismatches.

## Solution: OpenAPI as Single Source of Truth

```
specs/003-web-delivery/contracts/api.openapi.yaml (SOURCE OF TRUTH)
    │
    ├─→ Frontend: TypeScript types generated
    │   npm run generate-types
    │   → frontend/src/types/generated.ts
    │
    └─→ Backend: Runtime validation
        springdoc-openapi-starter-webmvc-ui
        → validates requests/responses at runtime
        → serves Swagger UI at /swagger-ui.html
```

## Implementation

### Backend Setup (Tasks T036a)

**Add to backend/pom.xml:**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

**Benefits:**
- Runtime validation of all API requests/responses against OpenAPI spec
- Automatic API documentation at http://localhost:8080/swagger-ui.html
- Catches type violations at runtime (fail fast)

### Frontend Setup (Tasks T036b-d)

**Add to frontend/package.json:**
```json
{
  "devDependencies": {
    "openapi-typescript": "^6.7.0"
  },
  "scripts": {
    "generate-types": "openapi-typescript ../../specs/003-web-delivery/contracts/api.openapi.yaml -o src/types/generated.ts",
    "prebuild": "npm run generate-types"
  }
}
```

**Benefits:**
- TypeScript types auto-generated from OpenAPI spec before every build
- No manual type maintenance required
- Compile-time type safety guaranteed

### Type Organization (Task T048b)

**frontend/src/types/api.ts** (manual file):
```typescript
// Re-export generated types
export type {
  SimulationTickRequest,
  SimulationTickResponse,
  PlatformState,
  Behavior,
  // ... all DTOs
} from './generated'

// Manual additions (not in OpenAPI spec):
export const API_BASE_URL = process.env.REACT_APP_API_URL || '/api'

export const SIMULATION_CONSTANTS = {
  DEFAULT_TICK_INTERVAL_MS: 100,
  DEFAULT_TIME_ACCELERATION: 1,
  MAX_TIME_ACCELERATION: 500,
  DEFAULT_WAYPOINT_TOLERANCE_M: 50,
  PORTLAND_HARBOUR_CENTER: { latitude: 50.6, longitude: -2.4 }
} as const

// Type guards for discriminated unions
export function isParallelTrackSearch(behavior: Behavior): behavior is ParallelTrackSearchBehavior {
  return behavior.type === 'PARALLEL_TRACK_SEARCH'
}

// Service interfaces for dependency injection
export interface SimulationService {
  tick(request: SimulationTickRequest): Promise<SimulationTickResponse>
  startSimulation(mission: Mission, platform: Platform): void
  pauseSimulation(): void
  resumeSimulation(): void
  stopSimulation(): void
}
```

**frontend/src/types/generated.ts** (auto-generated, DO NOT EDIT):
```typescript
// This file is auto-generated from OpenAPI spec
// Changes will be overwritten - edit api.openapi.yaml instead

export interface SimulationTickRequest {
  mission: Mission
  platformState: PlatformState
  behaviorState: BehaviorExecutionState
  deltaTime: number
  timeAcceleration?: number
}
// ... all other DTOs
```

## Workflow

### Development Workflow

1. **Update API contract**: Edit `specs/003-web-delivery/contracts/api.openapi.yaml`
2. **Regenerate types**: `cd frontend && npm run generate-types`
3. **TypeScript compiler catches issues**: Frontend won't compile if types mismatch
4. **Update Java DTOs**: Align backend DTOs with OpenAPI spec
5. **Runtime validation**: Spring validates requests at runtime

### Build Workflow

```bash
# Frontend build (automatic type generation)
cd frontend
npm run build
  → prebuild hook runs generate-types
  → TypeScript compiler uses generated types
  → Build fails if types incompatible

# Backend build (runtime validation)
cd backend
mvn spring-boot:run
  → Spring Boot starts with springdoc-openapi
  → All API requests validated against spec
  → Invalid requests return 400 Bad Request
```

### Testing Type Consistency

**Checkpoint Tasks (T047-T048b):**

1. **Generate types**: `cd frontend && npm run generate-types`
   - Verify `frontend/src/types/generated.ts` created
   - Check all DTOs present (SimulationTickRequest, PlatformState, etc.)

2. **Compare with manual types**:
   - Review `specs/003-web-delivery/contracts/types.ts`
   - Verify service interfaces compatible with generated DTOs
   - Ensure type guards work with generated types

3. **Validate backend**:
   ```bash
   cd backend && mvn spring-boot:run
   # Navigate to http://localhost:8080/swagger-ui.html
   # Verify all endpoints documented correctly
   ```

4. **Test round-trip**:
   ```bash
   # Frontend calls backend
   curl -X POST http://localhost:8080/api/simulation/tick \
     -H "Content-Type: application/json" \
     -d @test-request.json

   # Backend validates request against OpenAPI schema
   # Returns 400 if invalid, 200 with response if valid
   ```

## Benefits

✅ **Single source of truth**: OpenAPI spec defines contract
✅ **No manual sync**: Types auto-generated from spec
✅ **Compile-time safety**: TypeScript catches type errors before runtime
✅ **Runtime validation**: Spring validates all requests/responses
✅ **Documentation**: Swagger UI automatically generated
✅ **Breaking changes caught early**: Build fails if types incompatible

## Common Issues

### Issue: Generated types don't match backend DTOs

**Cause**: Backend DTOs don't match OpenAPI spec

**Fix**:
1. Compare backend DTO fields with OpenAPI schema
2. Update backend DTOs to match spec
3. OR update OpenAPI spec to match DTOs
4. Regenerate TypeScript types

### Issue: Frontend uses types not in OpenAPI spec

**Cause**: Service interfaces, type guards, constants not in OpenAPI (by design)

**Fix**: Keep these in manual `api.ts` file, not in generated types

### Issue: Build fails with "Property X does not exist"

**Cause**: TypeScript code uses property that doesn't exist in generated types

**Fix**:
1. Check if property exists in OpenAPI spec
2. Add to OpenAPI spec if missing
3. Regenerate types: `npm run generate-types`
4. If property shouldn't be in API, remove from frontend code

### Issue: Backend returns 400 "Validation failed"

**Cause**: Frontend sends data that doesn't match OpenAPI schema

**Fix**:
1. Check Swagger UI for expected schema
2. Update frontend to send correct data structure
3. OR update OpenAPI spec if frontend is correct

## References

- OpenAPI Specification: [specs/003-web-delivery/contracts/api.openapi.yaml](./contracts/api.openapi.yaml)
- Manual TypeScript Types: [specs/003-web-delivery/contracts/types.ts](./contracts/types.ts)
- openapi-typescript: https://github.com/drwpow/openapi-typescript
- springdoc-openapi: https://springdoc.org/
