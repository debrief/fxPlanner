import api from './api';

/**
 * Health check service.
 *
 * Monitors backend API availability.
 * Called every 5 seconds from App component.
 */

interface HealthResponse {
  status: string;
  timestamp: string;
  version: string;
  service: string;
}

/**
 * Check backend health status.
 *
 * @returns Health status object
 * @throws Error if health check fails
 */
export const checkHealth = async (): Promise<HealthResponse> => {
  const response = await api.get<HealthResponse>('/health');
  return response.data;
};
