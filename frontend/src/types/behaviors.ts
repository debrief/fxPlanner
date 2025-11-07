/**
 * Type definitions for USV mission behaviors
 */

/**
 * Geographic position with latitude and longitude
 */
export interface Position {
  lat: number;
  lon: number;
}

/**
 * Waypoint with position and speed
 */
export interface Waypoint extends Position {
  speed: number; // m/s
}

/**
 * Polygon defined by vertices
 */
export interface Polygon {
  vertices: Position[];
}

/**
 * Parameters for Parallel Track Search behavior
 */
export interface ParallelTrackParams {
  trackOrientation: number; // degrees (0-360)
  trackSpacing: number; // meters
  platformSpeed: number; // m/s
}

/**
 * Parameters for Expanding Square Search behavior
 */
export interface ExpandingSquareParams {
  initialDirection: number; // degrees (0-360)
  legIncrement: number; // meters
  platformSpeed: number; // m/s
}

/**
 * Waypoint Transit behavior configuration
 */
export interface WaypointTransitConfig {
  type: 'waypoint-transit';
  waypoints: Waypoint[];
}

/**
 * Parallel Track Search behavior configuration
 */
export interface ParallelTrackSearchConfig {
  type: 'parallel-track';
  polygon: Polygon;
  params: ParallelTrackParams;
  waypoints?: Waypoint[]; // Generated waypoints for the pattern
}

/**
 * Expanding Square Search behavior configuration
 */
export interface ExpandingSquareSearchConfig {
  type: 'expanding-square';
  polygon: Polygon;
  params: ExpandingSquareParams;
  waypoints?: Waypoint[]; // Generated waypoints for the pattern
}

/**
 * Return to Base behavior configuration
 */
export interface ReturnToBaseConfig {
  type: 'return-to-base';
  basePosition: Position;
  platformSpeed: number; // m/s
}

/**
 * Union type for all behavior configurations
 */
export type BehaviorConfig =
  | WaypointTransitConfig
  | ParallelTrackSearchConfig
  | ExpandingSquareSearchConfig
  | ReturnToBaseConfig;

/**
 * Behavior status
 */
export type BehaviorStatus = 'PENDING' | 'EXECUTING' | 'COMPLETE';

/**
 * Complete behavior representation in mission plan
 */
export interface Behavior {
  id: string;
  name: string;
  config: BehaviorConfig;
  status: BehaviorStatus;
  estimatedWaypoints?: number;
  estimatedDuration?: number; // seconds
}
