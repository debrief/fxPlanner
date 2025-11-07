/**
 * TypeScript Type Definitions for USV Mission Planner Web API
 *
 * CHECKPOINT: These types must be reviewed and approved before Phase 3 (frontend) implementation
 * Generated from OpenAPI specification and Java DTOs
 *
 * @version 1.0.0
 * @date 2025-11-05
 */

// ============================================================================
// Core Domain Types
// ============================================================================

export interface Mission {
  id: string; // UUID
  name: string; // max 100 chars
  description?: string; // max 500 chars
  createdAt: string; // ISO 8601
  behaviors: Behavior[];
  state: MissionState;
}

export interface Behavior {
  id: string; // UUID
  type: BehaviorType;
  name: string;
  description?: string;
  waypoints: Waypoint[];
  displayColor?: string; // hex color #RRGGBB

  // Type-specific properties (discriminated union preferred in implementation)
  searchArea?: Polygon; // For PARALLEL_TRACK_SEARCH, EXPANDING_SQUARE_SEARCH
  trackOrientation?: number; // 0-360 degrees, for PARALLEL_TRACK_SEARCH
  trackSpacing?: number; // meters, for PARALLEL_TRACK_SEARCH
  initialDirection?: number; // 0-360 degrees, for EXPANDING_SQUARE_SEARCH
  legIncrement?: number; // meters, for EXPANDING_SQUARE_SEARCH
  transitWaypoints?: Position[]; // for WAYPOINT_TRANSIT
  baseLocation?: Position; // for RETURN_TO_BASE
  platformSpeed?: number; // m/s, all behaviors
}

export interface BehaviorExecutionState {
  behaviorId: string; // UUID reference to Behavior
  currentWaypointIndex: number; // 0-based
  state: BehaviorState;
  lastDistanceToWaypoint?: number; // meters
  startTime?: string; // ISO 8601
  waypointsReached?: number;
}

// ============================================================================
// Platform Types
// ============================================================================

export interface Platform {
  id: string;
  name: string;
  capabilities: PlatformCapabilities;
  currentState: PlatformState;
  trackHistory?: PlatformState[];
}

export interface PlatformCapabilities {
  maxSpeed: number; // m/s (default 4.12 = 8 knots)
  turnRadius: number; // meters (default 200)
  acceleration: number; // m/s² (default 0.5)
  deceleration: number; // m/s² (default 1.0)
}

export interface PlatformState {
  position: Position;
  heading: number; // 0-360 degrees
  speed: number; // m/s
  depth: number; // meters (0 for surface)
  timestamp: string; // ISO 8601
}

export interface PlatformDemand {
  demandedHeading: number; // 0-360 degrees
  demandedSpeed: number; // m/s
  demandedDepth: number; // meters
  turnDirection: TurnDirection;
}

// ============================================================================
// Geographic Types
// ============================================================================

export interface Position {
  latitude: number; // decimal degrees [-90, 90]
  longitude: number; // decimal degrees [-180, 180]
}

export interface Waypoint {
  position: Position;
  speed: number; // m/s
  tolerance: number; // meters (acceptance radius, default 50)
  type: WaypointType;
}

export interface Polygon {
  vertices: Position[]; // min 3 vertices
  isClosed?: boolean; // default true
}

// ============================================================================
// API Request/Response Types
// ============================================================================

export interface SimulationTickRequest {
  mission: Mission;
  platformState: PlatformState;
  behaviorState: BehaviorExecutionState;
  deltaTime: number; // seconds (0.01 - 1.0)
  timeAcceleration?: number; // 1-500x (default 1)
}

export interface SimulationTickResponse {
  platformState: PlatformState; // Updated state
  behaviorState: BehaviorExecutionState; // Updated state
  demand: PlatformDemand; // For visualization
  behaviorComplete: boolean;
  missionComplete: boolean;
}

export interface ParallelTrackRequest {
  searchArea: Polygon;
  trackOrientation: number; // 0-360 degrees
  trackSpacing: number; // meters
  platformSpeed: number; // m/s
}

export interface ExpandingSquareRequest {
  searchArea: Polygon;
  initialDirection: number; // 0-360 degrees
  legIncrement: number; // meters
  platformSpeed: number; // m/s
}

export interface PatternResponse {
  waypoints: Waypoint[];
  estimatedDuration: number; // seconds
}

export interface HealthStatus {
  status: 'UP' | 'DOWN';
  timestamp: string; // ISO 8601
  version?: string;
}

export interface ApiError {
  code: string;
  message: string;
  details?: unknown;
}

// ============================================================================
// Enumerations
// ============================================================================

export enum MissionState {
  PLANNING = 'PLANNING',
  EXECUTING = 'EXECUTING',
  PAUSED = 'PAUSED',
  COMPLETE = 'COMPLETE'
}

export enum BehaviorType {
  PARALLEL_TRACK_SEARCH = 'PARALLEL_TRACK_SEARCH',
  EXPANDING_SQUARE_SEARCH = 'EXPANDING_SQUARE_SEARCH',
  WAYPOINT_TRANSIT = 'WAYPOINT_TRANSIT',
  RETURN_TO_BASE = 'RETURN_TO_BASE'
}

export enum BehaviorState {
  PENDING = 'PENDING',
  EXECUTING = 'EXECUTING',
  COMPLETE = 'COMPLETE'
}

export enum WaypointType {
  TRANSIT = 'TRANSIT',
  SEARCH = 'SEARCH',
  BASE = 'BASE'
}

export enum TurnDirection {
  PORT = 'PORT',
  STARBOARD = 'STARBOARD',
  SHORTEST = 'SHORTEST'
}

// ============================================================================
// Discriminated Unions (Type-Safe Behavior Variants)
// ============================================================================

export interface ParallelTrackSearchBehavior extends Omit<Behavior, 'type'> {
  type: BehaviorType.PARALLEL_TRACK_SEARCH;
  searchArea: Polygon;
  trackOrientation: number;
  trackSpacing: number;
  platformSpeed: number;
}

export interface ExpandingSquareSearchBehavior extends Omit<Behavior, 'type'> {
  type: BehaviorType.EXPANDING_SQUARE_SEARCH;
  searchArea: Polygon;
  initialDirection: number;
  legIncrement: number;
  platformSpeed: number;
}

export interface WaypointTransitBehavior extends Omit<Behavior, 'type'> {
  type: BehaviorType.WAYPOINT_TRANSIT;
  transitWaypoints: Position[];
  platformSpeed: number;
}

export interface ReturnToBaseBehavior extends Omit<Behavior, 'type'> {
  type: BehaviorType.RETURN_TO_BASE;
  baseLocation: Position;
  platformSpeed: number;
}

export type TypedBehavior =
  | ParallelTrackSearchBehavior
  | ExpandingSquareSearchBehavior
  | WaypointTransitBehavior
  | ReturnToBaseBehavior;

// ============================================================================
// Utility Types
// ============================================================================

/**
 * GeoJSON representation for mission serialization
 */
export interface MissionGeoJSON {
  type: 'FeatureCollection';
  properties: {
    mission: {
      id: string;
      name: string;
      createdAt: string;
      state: MissionState;
    };
  };
  features: GeoJSONFeature[];
}

export interface GeoJSONFeature {
  type: 'Feature';
  geometry: {
    type: 'Polygon' | 'LineString' | 'Point';
    coordinates: number[][] | number[][][] | number[];
  };
  properties: {
    behavior: Partial<Behavior>;
  };
}

// ============================================================================
// Service Interfaces (for dependency injection)
// ============================================================================

export interface SimulationService {
  tick(request: SimulationTickRequest): Promise<SimulationTickResponse>;
  startSimulation(mission: Mission, platform: Platform): void;
  pauseSimulation(): void;
  resumeSimulation(): void;
  stopSimulation(): void;
}

export interface MissionService {
  serializeToGeoJSON(mission: Mission): MissionGeoJSON;
  deserializeFromGeoJSON(geoJson: MissionGeoJSON): Mission;
  downloadMission(mission: Mission, filename: string): void;
  uploadMission(file: File): Promise<Mission>;
}

export interface BehaviorService {
  generateParallelTrackPattern(request: ParallelTrackRequest): Promise<PatternResponse>;
  generateExpandingSquarePattern(request: ExpandingSquareRequest): Promise<PatternResponse>;
  validateBehavior(behavior: Behavior): string[]; // Returns validation errors
}

// ============================================================================
// Constants
// ============================================================================

export const API_BASE_URL = process.env.REACT_APP_API_URL || '/api';

export const SIMULATION_CONSTANTS = {
  DEFAULT_TICK_INTERVAL_MS: 100, // 10 FPS minimum
  DEFAULT_TIME_ACCELERATION: 1,
  MAX_TIME_ACCELERATION: 500,
  DEFAULT_WAYPOINT_TOLERANCE_M: 50,
  DEFAULT_TURN_RADIUS_M: 200,
  DEFAULT_MAX_SPEED_MS: 4.12, // 8 knots
  PORTLAND_HARBOUR_CENTER: { latitude: 50.6, longitude: -2.4 }
} as const;

// ============================================================================
// Type Guards
// ============================================================================

export function isParallelTrackSearch(behavior: Behavior): behavior is ParallelTrackSearchBehavior {
  return behavior.type === BehaviorType.PARALLEL_TRACK_SEARCH;
}

export function isExpandingSquareSearch(behavior: Behavior): behavior is ExpandingSquareSearchBehavior {
  return behavior.type === BehaviorType.EXPANDING_SQUARE_SEARCH;
}

export function isWaypointTransit(behavior: Behavior): behavior is WaypointTransitBehavior {
  return behavior.type === BehaviorType.WAYPOINT_TRANSIT;
}

export function isReturnToBase(behavior: Behavior): behavior is ReturnToBaseBehavior {
  return behavior.type === BehaviorType.RETURN_TO_BASE;
}