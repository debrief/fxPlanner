import { useState, useEffect } from 'react';
import { Polygon, ParallelTrackParams } from '../../types/behaviors';
import { useMapContext } from '../../contexts/MapContext';
import { generateParallelTrack } from '../../services/PatternService';
import './BehaviorDialog.css';

interface ParallelTrackSearchDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: (polygon: Polygon, params: ParallelTrackParams) => void;
}

/**
 * Dialog for configuring Parallel Track Search behavior.
 * Allows user to draw search area and configure track parameters.
 */
const ParallelTrackSearchDialog: React.FC<ParallelTrackSearchDialogProps> = ({
  isOpen,
  onClose,
  onConfirm,
}) => {
  const { drawingMode, drawnPolygon, startPolygonDrawing, clearDrawing, setGeneratedWaypoints } = useMapContext();
  const [polygon, setPolygon] = useState<Polygon | null>(null);
  const [trackOrientation, setTrackOrientation] = useState<number>(0);
  const [trackSpacing, setTrackSpacing] = useState<number>(100); // 100m default
  const [platformSpeed, setPlatformSpeed] = useState<number>(4.0); // 4 m/s default
  const [isGenerating, setIsGenerating] = useState<boolean>(false);

  // Sync with map context polygon
  useEffect(() => {
    if (drawnPolygon && drawnPolygon.vertices.length >= 3) {
      setPolygon(drawnPolygon);
    }
  }, [drawnPolygon]);

  // Clear map drawing when dialog closes
  useEffect(() => {
    if (!isOpen) {
      clearDrawing();
    }
  }, [isOpen, clearDrawing]);

  if (!isOpen) return null;

  // Hide dialog during polygon drawing mode to allow map interaction
  if (drawingMode === 'polygon') return null;

  const handleDrawSearchArea = () => {
    startPolygonDrawing();
  };

  const handleAddTestPolygon = () => {
    // Add test polygon for development (rectangle in Portland Harbour area)
    const testPolygon: Polygon = {
      vertices: [
        { lat: 50.6, lon: -2.4 },
        { lat: 50.62, lon: -2.4 },
        { lat: 50.62, lon: -2.38 },
        { lat: 50.6, lon: -2.38 },
      ],
    };
    setPolygon(testPolygon);
  };

  const handleClearPolygon = () => {
    if (polygon && !window.confirm('Clear search area?')) {
      return;
    }
    setPolygon(null);
    clearDrawing();
  };

  const calculatePolygonArea = (): number => {
    if (!polygon || polygon.vertices.length < 3) return 0;

    // Simple area calculation (not accurate for large areas, but good enough for estimate)
    let area = 0;
    const vertices = polygon.vertices;
    for (let i = 0; i < vertices.length; i++) {
      const j = (i + 1) % vertices.length;
      area += vertices[i].lon * vertices[j].lat;
      area -= vertices[j].lon * vertices[i].lat;
    }
    area = Math.abs(area / 2);

    // Convert to square meters (rough approximation)
    return area * 111000 * 111000 * Math.cos((polygon.vertices[0].lat * Math.PI) / 180);
  };

  const estimateWaypoints = (): number => {
    if (!polygon || trackSpacing <= 0) return 0;

    const area = calculatePolygonArea();
    const numTracks = Math.ceil(Math.sqrt(area) / trackSpacing);
    const waypointsPerTrack = 4; // typical for rectangle

    return numTracks * waypointsPerTrack;
  };

  const estimateDuration = (): number => {
    if (!polygon || platformSpeed <= 0 || trackSpacing <= 0) return 0;

    const area = calculatePolygonArea();
    const totalDistance = (area / trackSpacing) * 1.2; // rough estimate with 20% overhead
    return totalDistance / platformSpeed;
  };

  const handleConfirm = async () => {
    if (!polygon) {
      console.log('Please draw a search area on the map');
      return;
    }

    if (trackOrientation < 0 || trackOrientation >= 360) {
      console.log('Track orientation must be between 0 and 360 degrees');
      return;
    }

    if (trackSpacing <= 0) {
      console.log('Track spacing must be greater than 0');
      return;
    }

    if (platformSpeed <= 0) {
      console.log('Platform speed must be greater than 0');
      return;
    }

    const params: ParallelTrackParams = {
      trackOrientation,
      trackSpacing,
      platformSpeed,
    };

    // Call backend API to generate pattern
    setIsGenerating(true);
    try {
      const result = await generateParallelTrack(
        polygon,
        trackOrientation,
        trackSpacing,
        platformSpeed
      );

      // Store generated waypoints in context
      setGeneratedWaypoints(result.waypoints);

      // Call parent confirm handler
      onConfirm(polygon, params);

      // Success message
      console.log(
        `Pattern generated successfully!\n` +
        `Waypoints: ${result.waypoints.length}\n` +
        `Estimated duration: ${Math.floor(result.estimatedDuration / 60)} min`
      );

      // Close dialog
      onClose();
    } catch (error: any) {
      // Show error to user
      console.log(`Failed to generate pattern: ${error.message}`);
      console.error('Pattern generation error:', error);
    } finally {
      setIsGenerating(false);
    }
  };

  const estimatedWaypoints = estimateWaypoints();
  const estimatedDuration = estimateDuration();

  return (
    <div className="dialog-overlay">
      <div className="dialog-container">
        <div className="dialog-header">
          <h2>Configure Parallel Track Search</h2>
          <button className="close-button" onClick={onClose}>×</button>
        </div>

        <div className="dialog-content">
          <div className="form-section">
            <h3>Search Area</h3>
            {!polygon ? (
              <div className="polygon-placeholder">
                <p className="placeholder-text">
                  No search area defined
                </p>
                <div className="button-group">
                  <button
                    className="secondary-button"
                    onClick={handleDrawSearchArea}
                  >
                    Draw Search Area on Map
                  </button>
                  <button className="secondary-button" onClick={handleAddTestPolygon}>
                    Use Test Polygon
                  </button>
                </div>
              </div>
            ) : (
              <div className="polygon-preview">
                <div className="polygon-info">
                  <div className="info-item">
                    <strong>Vertices:</strong> {polygon.vertices.length}
                  </div>
                  <div className="info-item">
                    <strong>Area:</strong> {(calculatePolygonArea() / 1000000).toFixed(2)} km²
                  </div>
                </div>
                <div className="polygon-vertices">
                  {polygon.vertices.map((vertex, index) => (
                    <div key={index} className="vertex-item">
                      Point {index + 1}: ({vertex.lat.toFixed(6)}, {vertex.lon.toFixed(6)})
                    </div>
                  ))}
                </div>
                <button className="secondary-button" onClick={handleClearPolygon}>
                  Clear Area
                </button>
              </div>
            )}
          </div>

          <div className="form-section">
            <h3>Track Parameters</h3>
            <div className="form-row">
              <label>
                Track Orientation (degrees):
                <input
                  type="number"
                  min="0"
                  max="360"
                  step="1"
                  value={trackOrientation}
                  onChange={(e) => setTrackOrientation(parseFloat(e.target.value) || 0)}
                />
                <span className="unit-hint">
                  (0° = North, 90° = East, 180° = South, 270° = West)
                </span>
              </label>
            </div>

            <div className="form-row">
              <label>
                Track Spacing (meters):
                <input
                  type="number"
                  min="1"
                  max="10000"
                  step="10"
                  value={trackSpacing}
                  onChange={(e) => setTrackSpacing(parseFloat(e.target.value) || 0)}
                />
                <span className="unit-hint">
                  Distance between parallel tracks
                </span>
              </label>
            </div>

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
            <h3>Estimated Pattern</h3>
            <div className="summary-item">
              <strong>Waypoints:</strong> ~{estimatedWaypoints}
            </div>
            <div className="summary-item">
              <strong>Duration:</strong> ~{Math.floor(estimatedDuration / 60)} min
            </div>
            <div className="summary-item">
              <strong>Coverage:</strong> {polygon ? (calculatePolygonArea() / 1000000).toFixed(2) : 0} km²
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
            disabled={!polygon || isGenerating}
          >
            {isGenerating ? 'Generating...' : 'Generate Pattern'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ParallelTrackSearchDialog;
