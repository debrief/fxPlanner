import { useState } from 'react';
import { Position } from '../../types/behaviors';
import './BehaviorPanel.css';

interface ReturnToBasePanelProps {
  onConfirm: (basePosition: Position, platformSpeed: number) => void;
  onCancel: () => void;
}

/**
 * Panel for configuring Return to Base behavior.
 * Allows user to select base location and set transit speed.
 * Embedded in tabbed panel (no modal wrapper).
 */
const ReturnToBasePanel: React.FC<ReturnToBasePanelProps> = ({
  onConfirm,
  onCancel,
}) => {
  const [basePosition, setBasePosition] = useState<Position | null>(null);
  const [platformSpeed, setPlatformSpeed] = useState<number>(4.0); // 4 m/s default

  // TODO: Implement map single-click mode for base selection
  // For now, provide manual entry and test button

  const handleUseCurrentLocation = () => {
    // Portland Harbour default
    setBasePosition({ lat: 50.6, lon: -2.4 });
  };

  const handleManualEntry = (lat: number, lon: number) => {
    setBasePosition({ lat, lon });
  };

  const handleConfirm = () => {
    if (!basePosition) {
      alert('Please select a base location');
      return;
    }

    if (platformSpeed <= 0) {
      alert('Platform speed must be greater than 0');
      return;
    }

    onConfirm(basePosition, platformSpeed);
  };

  return (
    <div className="behavior-panel">
      <div className="panel-header">
        <h3>Configure Return to Base</h3>
      </div>

      <div className="panel-content">
        <div className="form-section">
          <h3>Base Location</h3>
          {!basePosition ? (
            <div className="polygon-placeholder">
              <p className="placeholder-text">
                No base location selected
              </p>
              <div className="button-group">
                <button
                  className="secondary-button"
                  onClick={handleUseCurrentLocation}
                >
                  Use Portland Harbour
                </button>
              </div>
            </div>
          ) : (
            <div className="polygon-preview">
              <div className="polygon-info">
                <div className="info-item">
                  <strong>Latitude:</strong> {basePosition.lat.toFixed(6)}
                </div>
                <div className="info-item">
                  <strong>Longitude:</strong> {basePosition.lon.toFixed(6)}
                </div>
              </div>
              <button
                className="secondary-button"
                onClick={() => setBasePosition(null)}
              >
                Clear Location
              </button>
            </div>
          )}
        </div>

        <div className="form-section">
          <h3>Manual Entry</h3>
          <div className="form-row">
            <label>
              Latitude:
              <input
                type="number"
                min="-90"
                max="90"
                step="0.000001"
                value={basePosition?.lat || ''}
                onChange={(e) => {
                  const lat = parseFloat(e.target.value);
                  if (!isNaN(lat)) {
                    handleManualEntry(lat, basePosition?.lon || 0);
                  }
                }}
              />
            </label>
          </div>

          <div className="form-row">
            <label>
              Longitude:
              <input
                type="number"
                min="-180"
                max="180"
                step="0.000001"
                value={basePosition?.lon || ''}
                onChange={(e) => {
                  const lon = parseFloat(e.target.value);
                  if (!isNaN(lon)) {
                    handleManualEntry(basePosition?.lat || 0, lon);
                  }
                }}
              />
            </label>
          </div>
        </div>

        <div className="form-section">
          <h3>Transit Parameters</h3>
          <div className="form-row">
            <label>
              Platform Speed (m/s):
              <input
                type="number"
                min="0.1"
                max="20"
                step="0.1"
                value={platformSpeed}
                onChange={(e) => setPlatformSpeed(parseFloat(e.target.value) || 0)}
              />
              <span className="unit-hint">
                ({(platformSpeed * 1.94384).toFixed(1)} knots)
              </span>
            </label>
          </div>
        </div>

        <div className="summary-section">
          <h3>Summary</h3>
          <div className="summary-item">
            <strong>Base Location:</strong> {
              basePosition
                ? `${basePosition.lat.toFixed(4)}°N, ${Math.abs(basePosition.lon).toFixed(4)}°W`
                : 'Not set'
            }
          </div>
          <div className="summary-item">
            <strong>Transit Speed:</strong> {platformSpeed.toFixed(1)} m/s ({(platformSpeed * 1.94384).toFixed(1)} knots)
          </div>
        </div>
      </div>

      <div className="panel-footer">
        <button className="secondary-button" onClick={onCancel}>
          Cancel
        </button>
        <button
          className="primary-button"
          onClick={handleConfirm}
          disabled={!basePosition}
        >
          Create Behavior
        </button>
      </div>
    </div>
  );
};

export default ReturnToBasePanel;
