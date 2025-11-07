import { useState, useEffect } from 'react';
import { Waypoint } from '../../types/behaviors';
import { useMapContext } from '../../contexts/MapContext';
import './BehaviorDialog.css';

interface WaypointTransitDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: (waypoints: Waypoint[]) => void;
}

/**
 * Dialog for configuring Waypoint Transit behavior.
 * Allows user to add waypoints via map interaction and set speeds.
 */
const WaypointTransitDialog: React.FC<WaypointTransitDialogProps> = ({
  isOpen,
  onClose,
  onConfirm,
}) => {
  const [waypoints, setWaypoints] = useState<Waypoint[]>([]);
  const [defaultSpeed, setDefaultSpeed] = useState<number>(4.0); // 4 m/s default (~7.8 knots)
  const { drawingMode, drawnWaypoints, startWaypointDrawing, clearDrawing } = useMapContext();

  // Sync map waypoints to dialog when they change
  useEffect(() => {
    if (drawnWaypoints.length > 0 && isOpen) {
      // Append new waypoints from map to existing waypoints
      setWaypoints(prev => {
        // Only add waypoints that aren't already in the list
        const newWaypoints = drawnWaypoints.filter(
          dw => !prev.some(p => p.lat === dw.lat && p.lon === dw.lon)
        );
        return [...prev, ...newWaypoints];
      });
    }
  }, [drawnWaypoints, isOpen]);

  // Clear map drawing when dialog closes
  useEffect(() => {
    if (!isOpen) {
      clearDrawing();
    }
  }, [isOpen, clearDrawing]);

  if (!isOpen) return null;

  // Hide dialog during waypoint drawing mode to allow map interaction
  if (drawingMode === 'waypoint') return null;

  const handleAddWaypointsOnMap = () => {
    startWaypointDrawing();
  };

  const handleAddTestWaypoints = () => {
    // Add some test waypoints for development
    const testWaypoints: Waypoint[] = [
      { lat: 50.6, lon: -2.4, speed: defaultSpeed },
      { lat: 50.61, lon: -2.39, speed: defaultSpeed },
      { lat: 50.62, lon: -2.38, speed: defaultSpeed },
    ];
    setWaypoints([...waypoints, ...testWaypoints]);
  };

  const handleDeleteWaypoint = (index: number) => {
    setWaypoints(waypoints.filter((_, i) => i !== index));
  };

  const handleClearAll = () => {
    if (waypoints.length > 0 && !window.confirm('Clear all waypoints?')) {
      return;
    }
    setWaypoints([]);
  };

  const handleSpeedChange = (index: number, newSpeed: number) => {
    const updated = [...waypoints];
    updated[index].speed = newSpeed;
    setWaypoints(updated);
  };

  const calculateTotalDistance = (): number => {
    if (waypoints.length < 2) return 0;

    let total = 0;
    for (let i = 0; i < waypoints.length - 1; i++) {
      const dx = waypoints[i + 1].lon - waypoints[i].lon;
      const dy = waypoints[i + 1].lat - waypoints[i].lat;
      total += Math.sqrt(dx * dx + dy * dy) * 111000; // rough meters conversion
    }
    return total;
  };

  const calculateTotalDuration = (): number => {
    if (waypoints.length < 2) return 0;

    let total = 0;
    for (let i = 0; i < waypoints.length - 1; i++) {
      const dx = waypoints[i + 1].lon - waypoints[i].lon;
      const dy = waypoints[i + 1].lat - waypoints[i].lat;
      const distance = Math.sqrt(dx * dx + dy * dy) * 111000;
      const avgSpeed = (waypoints[i].speed + waypoints[i + 1].speed) / 2;
      total += distance / avgSpeed;
    }
    return total;
  };

  const handleConfirm = () => {
    if (waypoints.length === 0) {
      console.log('Please add at least one waypoint');
      return;
    }
    onConfirm(waypoints);
  };

  const totalDistance = calculateTotalDistance();
  const totalDuration = calculateTotalDuration();

  return (
    <div className="dialog-overlay">
      <div className="dialog-container">
        <div className="dialog-header">
          <h2>Configure Waypoint Transit</h2>
          <button className="close-button" onClick={onClose}>×</button>
        </div>

        <div className="dialog-content">
          <div className="form-section">
            <label>
              Default Speed (m/s):
              <input
                type="number"
                min="0.1"
                max="20"
                step="0.1"
                value={defaultSpeed}
                onChange={(e) => setDefaultSpeed(parseFloat(e.target.value) || 0)}
              />
              <span className="unit-hint">({(defaultSpeed * 1.94384).toFixed(1)} knots)</span>
            </label>
          </div>

          <div className="form-section">
            <div className="button-group">
              <button
                className="secondary-button"
                onClick={handleAddWaypointsOnMap}
              >
                Add from Map
              </button>
              <button className="secondary-button" onClick={handleAddTestWaypoints}>
                Add Test Waypoints
              </button>
              <button
                className="secondary-button"
                onClick={handleClearAll}
                disabled={waypoints.length === 0}
              >
                Clear All
              </button>
            </div>
          </div>

          <div className="waypoints-section">
            <h3>Waypoints ({waypoints.length})</h3>
            {waypoints.length === 0 ? (
              <p className="placeholder-text">No waypoints added yet.</p>
            ) : (
              <div className="waypoints-table-container">
                <table className="waypoints-table">
                  <thead>
                    <tr>
                      <th>#</th>
                      <th>Latitude</th>
                      <th>Longitude</th>
                      <th>Speed (m/s)</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {waypoints.map((wp, index) => (
                      <tr key={index}>
                        <td>{index + 1}</td>
                        <td>{wp.lat.toFixed(6)}</td>
                        <td>{wp.lon.toFixed(6)}</td>
                        <td>
                          <input
                            type="number"
                            min="0.1"
                            max="20"
                            step="0.1"
                            value={wp.speed}
                            onChange={(e) =>
                              handleSpeedChange(index, parseFloat(e.target.value) || 0)
                            }
                            className="speed-input"
                          />
                        </td>
                        <td>
                          <button
                            className="delete-button"
                            onClick={() => handleDeleteWaypoint(index)}
                          >
                            Delete
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          <div className="summary-section">
            <div className="summary-item">
              <strong>Total Distance:</strong> {(totalDistance / 1000).toFixed(2)} km
            </div>
            <div className="summary-item">
              <strong>Est. Duration:</strong> {Math.floor(totalDuration / 60)} min {Math.floor(totalDuration % 60)} sec
            </div>
          </div>
        </div>

        <div className="dialog-footer">
          <button className="secondary-button" onClick={onClose}>
            Cancel
          </button>
          <button
            className="primary-button"
            onClick={handleConfirm}
            disabled={waypoints.length === 0}
          >
            Create Behavior
          </button>
        </div>
      </div>
    </div>
  );
};

export default WaypointTransitDialog;
