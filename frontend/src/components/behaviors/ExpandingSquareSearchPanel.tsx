import { useState, useEffect } from 'react';
import { Polygon, ExpandingSquareParams, Waypoint } from '../../types/behaviors';
import { useMapContext } from '../../contexts/MapContext';
import { generateExpandingSquare } from '../../services/PatternService';
import './BehaviorPanel.css';

interface ExpandingSquareSearchPanelProps {
  onConfirm: (polygon: Polygon, params: ExpandingSquareParams, waypoints: Waypoint[], estimatedDuration: number) => void;
  onCancel: () => void;
}

/**
 * Panel for configuring Expanding Square Search behavior.
 * Allows user to draw search area and configure spiral parameters.
 * Embedded in tabbed panel (no modal wrapper).
 */
const ExpandingSquareSearchPanel: React.FC<ExpandingSquareSearchPanelProps> = ({
  onConfirm,
  onCancel,
}) => {
  const { drawnPolygon, startPolygonDrawing, clearDrawing, setGeneratedWaypoints } = useMapContext();
  const [polygon, setPolygon] = useState<Polygon | null>(null);
  const [initialDirection, setInitialDirection] = useState<number>(0);
  const [legIncrement, setLegIncrement] = useState<number>(50); // 50m default
  const [platformSpeed, setPlatformSpeed] = useState<number>(4.0); // 4 m/s default
  const [isGenerating, setIsGenerating] = useState<boolean>(false);

  // Sync with map context polygon
  useEffect(() => {
    if (drawnPolygon && drawnPolygon.vertices.length >= 3) {
      setPolygon(drawnPolygon);
    }
  }, [drawnPolygon]);

  // Clear map drawing when component unmounts
  useEffect(() => {
    return () => {
      clearDrawing();
    };
  }, [clearDrawing]);

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
    if (!polygon || legIncrement <= 0) return 0;

    const area = calculatePolygonArea();
    const sideLength = Math.sqrt(area);
    const numLegs = Math.ceil(sideLength / legIncrement);

    // Expanding square has 4 waypoints per complete square
    return numLegs * 4;
  };

  const estimateDuration = (): number => {
    if (!polygon || platformSpeed <= 0 || legIncrement <= 0) return 0;

    const area = calculatePolygonArea();
    const sideLength = Math.sqrt(area);
    // Spiral covers approximately the diagonal distance
    const totalDistance = sideLength * Math.sqrt(2) * 1.2; // 20% overhead
    return totalDistance / platformSpeed;
  };

  const handleConfirm = async () => {
    if (!polygon) {
      console.log('Please draw a search area on the map');
      return;
    }

    if (initialDirection < 0 || initialDirection >= 360) {
      console.log('Initial direction must be between 0 and 360 degrees');
      return;
    }

    if (legIncrement <= 0) {
      console.log('Leg increment must be greater than 0');
      return;
    }

    if (platformSpeed <= 0) {
      console.log('Platform speed must be greater than 0');
      return;
    }

    const params: ExpandingSquareParams = {
      initialDirection,
      legIncrement,
      platformSpeed,
    };

    // Call backend API to generate pattern
    setIsGenerating(true);
    try {
      const result = await generateExpandingSquare(
        polygon,
        initialDirection,
        legIncrement,
        platformSpeed
      );

      // Store generated waypoints in context
      setGeneratedWaypoints(result.waypoints);

      // Call parent confirm handler with generation result
      onConfirm(polygon, params, result.waypoints, result.estimatedDuration);

      // Success message
      console.log(
        `Pattern generated successfully!\n` +
        `Waypoints: ${result.waypoints.length}\n` +
        `Estimated duration: ${Math.floor(result.estimatedDuration / 60)} min`
      );

      // Close panel handled by parent
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
    <div className="behavior-panel">
      <div className="panel-header">
        <h3>Configure Expanding Square Search</h3>
      </div>

      <div className="panel-content">
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
          <h3>Spiral Parameters</h3>
          <div className="form-row">
            <label>
              Initial Direction (degrees):
              <input
                type="number"
                min="0"
                max="360"
                step="1"
                value={initialDirection}
                onChange={(e) => setInitialDirection(parseFloat(e.target.value) || 0)}
              />
              <span className="unit-hint">
                (0° = North, 90° = East, 180° = South, 270° = West)
              </span>
            </label>
          </div>

          <div className="form-row">
            <label>
              Leg Increment (meters):
              <input
                type="number"
                min="1"
                max="1000"
                step="10"
                value={legIncrement}
                onChange={(e) => setLegIncrement(parseFloat(e.target.value) || 0)}
              />
              <span className="unit-hint">
                Distance increase per spiral leg
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

      <div className="panel-footer">
        <button className="secondary-button" onClick={onCancel}>
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
  );
};

export default ExpandingSquareSearchPanel;
