import { useState, useEffect } from 'react';
import './StatePanel.css';
import { getSimulationService, SimulationState } from '../services/SimulationService';

/**
 * State panel component.
 *
 * Displays current platform state: position, heading, speed, depth.
 * Updates in real-time during simulation.
 */
const StatePanel: React.FC = () => {
  const [simState, setSimState] = useState<SimulationState | null>(null);

  useEffect(() => {
    const simService = getSimulationService();
    const unsubscribe = simService.subscribe((state) => {
      setSimState(state);
    });

    // Initialize state
    setSimState(simService.getState());

    return unsubscribe;
  }, []);

  const formatPosition = (lat: number, lon: number): string => {
    const latDir = lat >= 0 ? 'N' : 'S';
    const lonDir = lon >= 0 ? 'E' : 'W';
    return `${Math.abs(lat).toFixed(6)}°${latDir}, ${Math.abs(lon).toFixed(6)}°${lonDir}`;
  };

  const formatSpeed = (speedMs: number): string => {
    const knots = speedMs * 1.94384; // m/s to knots
    return `${speedMs.toFixed(2)} m/s (${knots.toFixed(2)} kn)`;
  };

  const platformState = simState?.platformState;
  const currentBehaviorIndex = simState?.currentBehaviorIndex ?? 0;
  const totalBehaviors = simState?.mission.behaviors.length ?? 0;
  const currentBehavior = simState?.mission.behaviors[currentBehaviorIndex];

  return (
    <div className="state-panel">
      <div className="panel-header">
        <h3>Platform State</h3>
      </div>
      <div className="panel-content">
        <div className="state-row">
          <span className="state-label">Position:</span>
          <span className="state-value">
            {platformState ? formatPosition(platformState.position.lat, platformState.position.lon) : '--'}
          </span>
        </div>
        <div className="state-row">
          <span className="state-label">Heading:</span>
          <span className="state-value">
            {platformState ? `${platformState.heading.toFixed(1)}°` : '--'}
          </span>
        </div>
        <div className="state-row">
          <span className="state-label">Speed:</span>
          <span className="state-value">
            {platformState ? formatSpeed(platformState.speed) : '--'}
          </span>
        </div>
        <div className="state-row">
          <span className="state-label">Depth:</span>
          <span className="state-value">
            {platformState ? `${platformState.depth.toFixed(1)} m` : '--'}
          </span>
        </div>
        <div className="state-row">
          <span className="state-label">Behavior:</span>
          <span className="state-value">
            {currentBehavior ? `${currentBehaviorIndex + 1}/${totalBehaviors}` : '--'}
          </span>
        </div>
        {currentBehavior && (
          <div className="state-row">
            <span className="state-label">Current:</span>
            <span className="state-value behavior-name">
              {currentBehavior.name}
            </span>
          </div>
        )}
      </div>
    </div>
  );
};

export default StatePanel;
