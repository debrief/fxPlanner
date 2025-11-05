import './MissionPlanPanel.css';

/**
 * Mission Plan panel component.
 *
 * Displays list of behaviors in the mission plan.
 * Allows adding, removing, editing, and reordering behaviors.
 *
 * Placeholder for Phase 5 (User Story 2 - Configure Mission).
 */
const MissionPlanPanel: React.FC = () => {
  return (
    <div className="mission-plan-panel">
      <div className="panel-header">
        <h3>Mission Plan</h3>
      </div>
      <div className="panel-content">
        <p className="placeholder-text">No behaviors added yet.</p>
        <p className="hint-text">Click "Add Behavior" to start building your mission.</p>
      </div>
    </div>
  );
};

export default MissionPlanPanel;
