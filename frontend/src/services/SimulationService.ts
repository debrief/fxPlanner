import { Behavior } from '../types/behaviors';

export interface PlatformState {
  position: { lat: number; lon: number };
  heading: number;
  speed: number;
  depth: number;
  timestamp: string;
}

export interface SimulationState {
  mission: {
    behaviors: Behavior[];
  };
  platformState: PlatformState;
  currentBehaviorIndex: number;
  running: boolean;
  paused: boolean;
  timeAcceleration: number;
  simulationTime: number; // elapsed time in seconds
}

export class SimulationService {
  private state: SimulationState;
  private animationFrameId: number | null = null;
  private lastTickTime: number = 0;
  private subscribers: Array<(state: SimulationState) => void> = [];
  private currentWaypointIndex: number = 0;

  constructor() {
    this.state = {
      mission: { behaviors: [] },
      platformState: {
        position: { lat: 50.6, lon: -2.4 }, // Portland Harbour default
        heading: 0,
        speed: 0,
        depth: 0,
        timestamp: new Date().toISOString(),
      },
      currentBehaviorIndex: 0,
      running: false,
      paused: false,
      timeAcceleration: 1,
      simulationTime: 0,
    };
  }

  subscribe(callback: (state: SimulationState) => void): () => void {
    this.subscribers.push(callback);
    // Return unsubscribe function
    return () => {
      this.subscribers = this.subscribers.filter((cb) => cb !== callback);
    };
  }

  private notifySubscribers() {
    // Pass a copy so React detects changes
    this.subscribers.forEach((callback) => callback({ ...this.state }));
  }

  loadMission(behaviors: Behavior[]) {
    this.state.mission.behaviors = behaviors;

    // Initialize platform at first waypoint if available
    if (behaviors.length > 0) {
      const firstBehavior = behaviors[0];
      const config = firstBehavior.config;

      // Type guard: check if config has waypoints
      let firstWaypoint;
      if (config.type === 'waypoint-transit' || config.type === 'parallel-track' || config.type === 'expanding-square') {
        firstWaypoint = config.waypoints?.[0];
      } else if (config.type === 'return-to-base') {
        firstWaypoint = config.basePosition;
      }

      if (firstWaypoint) {
        // Start platform slightly offset from first waypoint (0.01 degrees south) so it has to move
        this.state.platformState = {
          position: { lat: firstWaypoint.lat - 0.01, lon: firstWaypoint.lon },
          heading: 0,
          speed: 0,
          depth: 0,
          timestamp: new Date().toISOString(),
        };
      }
    }

    this.state.currentBehaviorIndex = 0;
    this.state.simulationTime = 0;
    this.currentWaypointIndex = 0; // Reset waypoint index
    this.notifySubscribers();
  }

  start() {
    console.log('[SimulationService] start() called, behaviors:', this.state.mission.behaviors.length);
    if (this.state.mission.behaviors.length === 0) {
      throw new Error('Cannot start simulation: no behaviors in mission');
    }

    this.state.running = true;
    this.state.paused = false;
    this.lastTickTime = performance.now();
    console.log('[SimulationService] State updated - running:', this.state.running, 'paused:', this.state.paused);
    console.log('[SimulationService] Starting tick loop');
    this.tick();
    console.log('[SimulationService] Notifying', this.subscribers.length, 'subscribers');
    this.notifySubscribers();
  }

  pause() {
    this.state.paused = true;
    if (this.animationFrameId !== null) {
      cancelAnimationFrame(this.animationFrameId);
      this.animationFrameId = null;
    }
    this.notifySubscribers();
  }

  resume() {
    if (!this.state.running || !this.state.paused) return;

    this.state.paused = false;
    this.lastTickTime = performance.now();
    this.tick();
    this.notifySubscribers();
  }

  stop() {
    this.state.running = false;
    this.state.paused = false;
    if (this.animationFrameId !== null) {
      cancelAnimationFrame(this.animationFrameId);
      this.animationFrameId = null;
    }

    // Reset to initial position
    this.loadMission(this.state.mission.behaviors);
  }

  setTimeAcceleration(acceleration: number) {
    this.state.timeAcceleration = Math.max(1, Math.min(500, acceleration));
    this.notifySubscribers();
  }

  getState(): SimulationState {
    return { ...this.state };
  }

  private tick = () => {
    if (!this.state.running || this.state.paused) return;

    const now = performance.now();
    const deltaTimeMs = now - this.lastTickTime;
    const deltaTime = (deltaTimeMs / 1000) * this.state.timeAcceleration;
    this.lastTickTime = now;

    // Update simulation time
    this.state.simulationTime += deltaTime;

    // Simple stub simulation: move platform slowly (backend will do real simulation)
    // This is just for visualization - Phase 7 will integrate backend POST /simulation/tick
    this.updatePlatformStub(deltaTime);

    this.notifySubscribers();

    // Continue loop
    this.animationFrameId = requestAnimationFrame(this.tick);
  };

  private updatePlatformStub(deltaTime: number) {
    // Stub: slowly move platform in simple pattern for visualization
    // Real implementation will call backend /api/simulation/tick

    const currentBehavior = this.state.mission.behaviors[this.state.currentBehaviorIndex];
    if (!currentBehavior) return;

    const config = currentBehavior.config;

    // Type guard: get waypoints based on config type
    let waypoints;
    if (config.type === 'waypoint-transit' || config.type === 'parallel-track' || config.type === 'expanding-square') {
      waypoints = config.waypoints;
    } else if (config.type === 'return-to-base') {
      waypoints = [config.basePosition]; // Treat base position as single waypoint
    }

    if (!waypoints || waypoints.length === 0) {
      return;
    }

    // Check if we've completed all waypoints
    if (this.currentWaypointIndex >= waypoints.length) {
      // All waypoints complete - stop simulation
      this.stop();
      return;
    }

    // Move towards current target waypoint
    const targetWaypoint = waypoints[this.currentWaypointIndex];
    const currentPos = this.state.platformState.position;

    // Calculate heading towards target
    const dLat = targetWaypoint.lat - currentPos.lat;
    const dLon = targetWaypoint.lon - currentPos.lon;
    const heading = (Math.atan2(dLon, dLat) * 180 / Math.PI + 360) % 360;

    // Move slowly towards target (stub - real dynamics in backend)
    const speed = 2.0; // m/s
    const distancePerSecond = speed / 111000; // rough degrees per second
    const distance = distancePerSecond * deltaTime;

    const totalDistance = Math.sqrt(dLat * dLat + dLon * dLon);
    if (totalDistance > distance) {
      const fraction = distance / totalDistance;
      // Create new position object to trigger React re-render
      this.state.platformState = {
        ...this.state.platformState,
        position: {
          lat: currentPos.lat + dLat * fraction,
          lon: currentPos.lon + dLon * fraction,
        },
        heading,
        speed,
        timestamp: new Date().toISOString(),
      };
    } else {
      // Reached waypoint - advance to next
      this.currentWaypointIndex++;
      console.log(`[SimulationService] Reached waypoint ${this.currentWaypointIndex}/${waypoints.length}`);

      this.state.platformState = {
        ...this.state.platformState,
        position: { lat: targetWaypoint.lat, lon: targetWaypoint.lon },
        speed: this.currentWaypointIndex < waypoints.length ? speed : 0, // Keep moving if more waypoints
        timestamp: new Date().toISOString(),
      };
    }
  }
}

// Singleton instance
let simulationServiceInstance: SimulationService | null = null;

export const getSimulationService = (): SimulationService => {
  if (!simulationServiceInstance) {
    simulationServiceInstance = new SimulationService();
  }
  return simulationServiceInstance;
};
