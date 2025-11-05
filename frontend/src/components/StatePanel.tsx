import './StatePanel.css';

/**
 * State panel component.
 *
 * Displays current platform state: position, heading, speed, depth.
 * Updates in real-time during simulation.
 *
 * Placeholder for Phase 6 (User Story 3 - Execute Simulation).
 */
const StatePanel: React.FC = () => {
  return (
    <div className="state-panel">
      <div className="panel-header">
        <h3>Platform State</h3>
      </div>
      <div className="panel-content">
        <div className="state-row">
          <span className="state-label">Position:</span>
          <span className="state-value">--</span>
        </div>
        <div className="state-row">
          <span className="state-label">Heading:</span>
          <span className="state-value">--</span>
        </div>
        <div className="state-row">
          <span className="state-label">Speed:</span>
          <span className="state-value">--</span>
        </div>
        <div className="state-row">
          <span className="state-label">Depth:</span>
          <span className="state-value">--</span>
        </div>
      </div>
    </div>
  );
};

export default StatePanel;
