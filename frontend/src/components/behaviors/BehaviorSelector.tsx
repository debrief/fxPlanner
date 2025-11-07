import { useState } from 'react';
import './BehaviorSelector.css';

export type BehaviorType = 'parallel-track' | 'expanding-square' | 'waypoint-transit' | 'return-to-base';

interface BehaviorSelectorProps {
  onConfigureRequest: (behaviorType: BehaviorType) => void;
}

/**
 * Behavior selector component with dropdown and Configure button.
 * Used to initiate behavior configuration workflow.
 */
const BehaviorSelector: React.FC<BehaviorSelectorProps> = ({ onConfigureRequest }) => {
  const [selectedType, setSelectedType] = useState<string>('');

  const behaviorTypes = [
    { value: '', label: 'Select behavior type...' },
    { value: 'parallel-track', label: 'Parallel Track Search' },
    { value: 'expanding-square', label: 'Expanding Square Search' },
    { value: 'waypoint-transit', label: 'Waypoint Transit' },
    { value: 'return-to-base', label: 'Return to Base' },
  ];

  const handleConfigure = () => {
    if (selectedType) {
      onConfigureRequest(selectedType as BehaviorType);
      setSelectedType(''); // Reset after opening dialog
    }
  };

  return (
    <div className="behavior-selector">
      <select
        className="behavior-type-dropdown"
        value={selectedType}
        onChange={(e) => setSelectedType(e.target.value)}
      >
        {behaviorTypes.map(type => (
          <option key={type.value} value={type.value}>
            {type.label}
          </option>
        ))}
      </select>
      <button
        className="configure-button"
        disabled={!selectedType}
        onClick={handleConfigure}
      >
        Configure
      </button>
    </div>
  );
};

export default BehaviorSelector;
