import axios, { AxiosInstance, AxiosError } from 'axios';

/**
 * Axios instance configured for USV Mission Planner API.
 *
 * Base URL: /api (proxied to http://localhost:8080 in development)
 * Error interceptor: Logs errors and provides consistent error handling
 */
const api: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Response interceptor for error handling.
 */
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response) {
      // Server responded with error status
      console.error('API Error:', error.response.status, error.response.data);
    } else if (error.request) {
      // Request made but no response received
      console.error('Network Error: No response from server');
    } else {
      // Error in request setup
      console.error('Request Error:', error.message);
    }
    return Promise.reject(error);
  }
);

export default api;
