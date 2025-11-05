import './ControlPanel.css';

/**
 * Control panel component.
 *
 * Contains simulation controls: Start, Pause, Stop, Speed slider.
 *
 * Placeholder for Phase 6 (User Story 3 - Execute Simulation).
 */
const ControlPanel: React.FC = () => {
  return (
    <div className="control-panel">
      <div className="panel-header">
        <h3>Controls</h3>
      </div>
      <div className="panel-content">
        <button className="control-button" disabled>Start</button>
        <button className="control-button" disabled>Pause</button>
        <button className="control-button" disabled>Stop</button>
        <div className="speed-control">
          <label>Speed: 1x</label>
          <input type="range" min="1" max="500" value="1" disabled />
        </div>
      </div>
    </div>
  );
};

export default ControlPanel;
