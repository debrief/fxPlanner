import { useState, useRef } from 'react';
import { Behavior } from '../types/behaviors';
import { downloadMission, uploadMission } from '../services/MissionService';
import './MissionPlanPanel.css';

interface MissionPlanPanelProps {
  behaviors: Behavior[];
  onAddBehavior: (type: string) => void;
  onDeleteBehavior: (behaviorId: string) => void;
  onMoveBehavior: (behaviorId: string, direction: 'up' | 'down') => void;
  onLoadMission: (behaviors: Behavior[]) => void;
}

/**
 * Mission Plan panel component.
 *
 * Displays list of behaviors in the mission plan.
 * Allows adding, removing, editing, and reordering behaviors.
 */
const MissionPlanPanel: React.FC<MissionPlanPanelProps> = ({ behaviors, onAddBehavior, onDeleteBehavior, onMoveBehavior, onLoadMission }) => {
  const [selectedBehaviorType, setSelectedBehaviorType] = useState<string>('');
  const fileInputRef = useRef<HTMLInputElement>(null);

  const behaviorTypes = [
    { value: '', label: 'Select behavior type...' },
    { value: 'parallel-track', label: 'Parallel Track Search' },
    { value: 'expanding-square', label: 'Expanding Square Search' },
    { value: 'waypoint-transit', label: 'Waypoint Transit' },
    { value: 'return-to-base', label: 'Return to Base' },
  ];

  const handleAddBehavior = () => {
    if (selectedBehaviorType) {
      console.log('Adding behavior tab for:', selectedBehaviorType);
      onAddBehavior(selectedBehaviorType);
      // Reset selector after adding
      setSelectedBehaviorType('');
    }
  };

  const handleSaveMission = () => {
    if (behaviors.length === 0) {
      alert('No behaviors to save. Add at least one behavior to the mission plan.');
      return;
    }

    downloadMission(behaviors);
    alert('Mission saved successfully!');
  };

  const handleLoadMission = () => {
    fileInputRef.current?.click();
  };

  const handleFileSelect = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    try {
      const mission = await uploadMission(file);
      onLoadMission(mission.behaviors);
      alert(`Mission "${mission.name}" loaded successfully with ${mission.behaviors.length} behavior(s)!`);
    } catch (error: any) {
      alert(`Error loading mission: ${error.message}`);
      console.error('Mission load error:', error);
    }

    // Reset file input
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
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
            title="Add Behavior"
          >
            +
          </button>
          <fieldset className="mission-fieldset">
            <legend>Mission</legend>
            <button className="load-button" onClick={handleLoadMission}>Load</button>
            <button className="save-button" onClick={handleSaveMission}>Save</button>
          </fieldset>
          <input
            type="file"
            ref={fileInputRef}
            accept=".json"
            style={{ display: 'none' }}
            onChange={handleFileSelect}
          />
        </div>
      </div>
      <div className="panel-content">
        {behaviors.length === 0 ? (
          <>
            <p className="placeholder-text">No behaviors added yet.</p>
            <p className="hint-text">Select a behavior type and click "Add Behavior" to start building your mission.</p>
          </>
        ) : (
          <div className="behavior-list">
            {behaviors.map((behavior, index) => (
              <div key={behavior.id} className="behavior-item">
                <div className="behavior-number">{index + 1}</div>
                <div className="behavior-info">
                  <div className="behavior-name">{behavior.name}</div>
                  <div className="behavior-details">
                    {behavior.estimatedWaypoints && (
                      <span className="detail-item">
                        {behavior.estimatedWaypoints} waypoint{behavior.estimatedWaypoints !== 1 ? 's' : ''}
                      </span>
                    )}
                    {behavior.estimatedDuration && (
                      <span className="detail-item">
                        ~{Math.floor(behavior.estimatedDuration / 60)} min
                      </span>
                    )}
                    <span className="detail-item status-badge">{behavior.status}</span>
                  </div>
                </div>
                <div className="behavior-actions">
                  <button
                    className="action-button"
                    onClick={() => onMoveBehavior(behavior.id, 'up')}
                    disabled={index === 0}
                    title="Move up"
                  >
                    ↑
                  </button>
                  <button
                    className="action-button"
                    onClick={() => onMoveBehavior(behavior.id, 'down')}
                    disabled={index === behaviors.length - 1}
                    title="Move down"
                  >
                    ↓
                  </button>
                  <button
                    className="action-button delete-button"
                    onClick={() => {
                      if (window.confirm(`Delete "${behavior.name}"?`)) {
                        onDeleteBehavior(behavior.id);
                      }
                    }}
                    title="Delete"
                  >
                    🗑️
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default MissionPlanPanel;
