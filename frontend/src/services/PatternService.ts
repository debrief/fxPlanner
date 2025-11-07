import api from './api';
import { Polygon, Waypoint } from '../types/behaviors';

/**
 * Pattern generation service.
 *
 * Integrates with backend API to generate search patterns.
 * Converts between frontend and backend types.
 */

/**
 * Backend WaypointDTO structure (from API response)
 */
interface WaypointDTO {
  position: {
    latitude: number;
    longitude: number;
  };
  speed: number;
  acceptanceRadius: number;
  type: string;
}

/**
 * Backend position structure (for API request)
 */
interface PositionDTO {
  latitude: number;
  longitude: number;
}

/**
 * Backend polygon structure (for API request)
 */
interface PolygonDTO {
  vertices: PositionDTO[];
  isClosed: boolean;
}

/**
 * Request body for parallel track generation
 */
interface ParallelTrackRequest {
  searchArea: PolygonDTO;
  trackOrientation: number;
  trackSpacing: number;
  platformSpeed: number;
}

/**
 * Response from parallel track generation
 */
interface ParallelTrackResponse {
  waypoints: WaypointDTO[];
  estimatedDuration: number;
}

/**
 * Request body for expanding square generation
 */
interface ExpandingSquareRequest {
  searchArea: PolygonDTO;
  initialDirection: number;
  legIncrement: number;
  platformSpeed: number;
}

/**
 * Response from expanding square generation
 */
interface ExpandingSquareResponse {
  waypoints: WaypointDTO[];
  estimatedDuration: number;
}

/**
 * Generate parallel track search pattern.
 *
 * @param polygon Search area polygon
 * @param trackOrientation Track orientation in degrees (0-360)
 * @param trackSpacing Track spacing in meters
 * @param platformSpeed Platform speed in m/s
 * @returns Generated waypoints and estimated duration
 * @throws Error if API call fails or validation fails
 */
export const generateParallelTrack = async (
  polygon: Polygon,
  trackOrientation: number,
  trackSpacing: number,
  platformSpeed: number
): Promise<{ waypoints: Waypoint[]; estimatedDuration: number }> => {
  try {
    // Convert frontend polygon to backend DTO
    const searchArea: PolygonDTO = {
      vertices: polygon.vertices.map(v => ({
        latitude: v.lat,
        longitude: v.lon,
      })),
      isClosed: true,
    };

    // Build request
    const request: ParallelTrackRequest = {
      searchArea,
      trackOrientation,
      trackSpacing,
      platformSpeed,
    };

    // Call backend API
    const response = await api.post<ParallelTrackResponse>(
      '/behaviors/parallel-track/generate',
      request
    );

    // Convert backend DTOs to frontend waypoints
    const waypoints: Waypoint[] = response.data.waypoints.map(dto => ({
      lat: dto.position.latitude,
      lon: dto.position.longitude,
      speed: dto.speed,
    }));

    return {
      waypoints,
      estimatedDuration: response.data.estimatedDuration,
    };
  } catch (error: any) {
    // Enhanced error handling
    if (error.response) {
      const status = error.response.status;
      const data = error.response.data;

      if (status === 400) {
        throw new Error(`Invalid request: ${data.message || 'Bad request'}`);
      } else if (status === 500) {
        throw new Error(`Server error: ${data.message || 'Internal server error'}`);
      } else {
        throw new Error(`API error (${status}): ${data.message || 'Unknown error'}`);
      }
    } else if (error.request) {
      throw new Error('Network error: Unable to reach backend server');
    } else {
      throw new Error(`Request failed: ${error.message}`);
    }
  }
};

/**
 * Generate expanding square search pattern.
 *
 * @param polygon Search area polygon
 * @param initialDirection Initial direction in degrees (0-360)
 * @param legIncrement Leg increment in meters
 * @param platformSpeed Platform speed in m/s
 * @returns Generated waypoints and estimated duration
 * @throws Error if API call fails or validation fails
 */
export const generateExpandingSquare = async (
  polygon: Polygon,
  initialDirection: number,
  legIncrement: number,
  platformSpeed: number
): Promise<{ waypoints: Waypoint[]; estimatedDuration: number }> => {
  try {
    // Convert frontend polygon to backend DTO
    const searchArea: PolygonDTO = {
      vertices: polygon.vertices.map(v => ({
        latitude: v.lat,
        longitude: v.lon,
      })),
      isClosed: true,
    };

    // Build request
    const request: ExpandingSquareRequest = {
      searchArea,
      initialDirection,
      legIncrement,
      platformSpeed,
    };

    // Call backend API
    const response = await api.post<ExpandingSquareResponse>(
      '/behaviors/expanding-square/generate',
      request
    );

    // Convert backend DTOs to frontend waypoints
    const waypoints: Waypoint[] = response.data.waypoints.map(dto => ({
      lat: dto.position.latitude,
      lon: dto.position.longitude,
      speed: dto.speed,
    }));

    return {
      waypoints,
      estimatedDuration: response.data.estimatedDuration,
    };
  } catch (error: any) {
    // Enhanced error handling
    if (error.response) {
      const status = error.response.status;
      const data = error.response.data;

      if (status === 400) {
        throw new Error(`Invalid request: ${data.message || 'Bad request'}`);
      } else if (status === 500) {
        throw new Error(`Server error: ${data.message || 'Internal server error'}`);
      } else {
        throw new Error(`API error (${status}): ${data.message || 'Unknown error'}`);
      }
    } else if (error.request) {
      throw new Error('Network error: Unable to reach backend server');
    } else {
      throw new Error(`Request failed: ${error.message}`);
    }
  }
};
