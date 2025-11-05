import { useState } from 'react';
import './MissionPlanPanel.css';

/**
 * Mission Plan panel component.
 *
 * Displays list of behaviors in the mission plan.
 * Allows adding, removing, editing, and reordering behaviors.
 */
const MissionPlanPanel: React.FC = () => {
  const [selectedBehaviorType, setSelectedBehaviorType] = useState<string>('');

  const behaviorTypes = [
    { value: '', label: 'Select behavior type...' },
    { value: 'parallel-track', label: 'Parallel Track Search' },
    { value: 'expanding-square', label: 'Expanding Square Search' },
    { value: 'waypoint-transit', label: 'Waypoint Transit' },
    { value: 'return-to-base', label: 'Return to Base' },
  ];

  const handleAddBehavior = () => {
    if (selectedBehaviorType) {
      // TODO: Phase 5 - Open configuration dialog
      console.log('Adding behavior:', selectedBehaviorType);

      // Temporary feedback for Phase 4
      const behaviorName = behaviorTypes.find(b => b.value === selectedBehaviorType)?.label;
      alert(`Phase 5 TODO: Open configuration dialog for "${behaviorName}"\n\nFor Phase 4, this button confirms the UI is working. Configuration dialogs will be implemented in Phase 5.`);
    }
  };

  return (
    <div className="mission-plan-panel">
      <div className="panel-header">
        <h3>Mission Plan</h3>
        <div className="toolbar">
          <select
            className="behavior-selector"
            value={selectedBehaviorType}
            onChange={(e) => setSelectedBehaviorType(e.target.value)}
          >
            {behaviorTypes.map(type => (
              <option key={type.value} value={type.value}>
                {type.label}
              </option>
            ))}
          </select>
          <button
            className="add-button"
            disabled={!selectedBehaviorType}
            onClick={handleAddBehavior}
          >
            Add Behavior
          </button>
          <button className="load-button" disabled>Load Mission</button>
          <button className="save-button" disabled>Save Mission</button>
        </div>
      </div>
      <div className="panel-content">
        <p className="placeholder-text">No behaviors added yet.</p>
        <p className="hint-text">Select a behavior type and click "Add Behavior" to start building your mission.</p>
      </div>
    </div>
  );
};

export default MissionPlanPanel;
