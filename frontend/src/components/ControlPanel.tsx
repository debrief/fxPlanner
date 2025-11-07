import { useState, useEffect } from 'react';
import './ControlPanel.css';
import { getSimulationService, SimulationState } from '../services/SimulationService';

interface ControlPanelProps {
  hasBehaviors: boolean;
}

/**
 * Control panel component.
 *
 * Contains simulation controls: Start, Pause, Resume, Stop, Speed slider.
 */
const ControlPanel: React.FC<ControlPanelProps> = ({ hasBehaviors }) => {
  const [simState, setSimState] = useState<SimulationState | null>(null);
  const [timeAcceleration, setTimeAcceleration] = useState<number>(1);

  useEffect(() => {
    console.log('[ControlPanel] Setting up subscription to simulation service');
    const simService = getSimulationService();
    const unsubscribe = simService.subscribe((state) => {
      console.log('[ControlPanel] Received state update - running:', state.running, 'paused:', state.paused);
      setSimState(state);
      setTimeAcceleration(state.timeAcceleration);
    });

    // Initialize state
    const initialState = simService.getState();
    console.log('[ControlPanel] Initial state - running:', initialState.running, 'behaviors:', initialState.mission.behaviors.length);
    setSimState(initialState);

    return unsubscribe;
  }, []);

  const handleStart = () => {
    console.log('[ControlPanel] Start button clicked');
    try {
      console.log('[ControlPanel] Calling simulation service start()');
      getSimulationService().start();
      console.log('[ControlPanel] Start() returned successfully');
    } catch (error: any) {
      console.error('[ControlPanel] Start error:', error);
    }
  };

  const handlePause = () => {
    getSimulationService().pause();
  };

  const handleResume = () => {
    getSimulationService().resume();
  };

  const handleStop = () => {
    getSimulationService().stop();
  };

  const handleSpeedChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const newSpeed = parseFloat(event.target.value);
    setTimeAcceleration(newSpeed);
    getSimulationService().setTimeAcceleration(newSpeed);
  };

  const formatTime = (seconds: number): string => {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = Math.floor(seconds % 60);
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const isRunning = simState?.running && !simState?.paused;
  const isPaused = simState?.paused;
  const canStart = hasBehaviors && !simState?.running;
  const canPause = isRunning;
  const canResume = isPaused;
  const canStop = simState?.running;

  return (
    <div className="control-panel">
      <div className="panel-header">
        <h3>Time Control v2</h3>
      </div>
      <div className="panel-content">
        <div className="control-buttons">
          <button
            className="control-button"
            onClick={handleStart}
            disabled={!canStart}
            title={!hasBehaviors ? 'Add behaviors to mission first' : 'Start simulation'}
          >
            Start
          </button>
          <button
            className="control-button"
            onClick={handlePause}
            disabled={!canPause}
          >
            Pause
          </button>
          <button
            className="control-button"
            onClick={handleResume}
            disabled={!canResume}
          >
            Resume
          </button>
          <button
            className="control-button"
            onClick={handleStop}
            disabled={!canStop}
          >
            Stop
          </button>
        </div>
        <div className="speed-control">
          <label>Speed: {timeAcceleration}x</label>
          <input
            type="range"
            min="1"
            max="500"
            value={timeAcceleration}
            onChange={handleSpeedChange}
            disabled={!simState?.running}
          />
        </div>
        <div className="time-display">
          <label>Simulation Time:</label>
          <span className="time-value">{formatTime(simState?.simulationTime || 0)}</span>
        </div>
      </div>
    </div>
  );
};

export default ControlPanel;
