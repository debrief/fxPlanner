import React, { useEffect, useState } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polyline, Polygon as LeafletPolygon, useMapEvents } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import './MapPanel.css';
import { useMapContext } from '../contexts/MapContext';
import { Position, Waypoint, Behavior } from '../types/behaviors';
import { getSimulationService, SimulationState } from '../services/SimulationService';

// Fix for default marker icons in React Leaflet
import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';

let DefaultIcon = L.icon({
  iconUrl: icon,
  shadowUrl: iconShadow,
  iconSize: [25, 41],
  iconAnchor: [12, 41]
});

L.Marker.prototype.options.icon = DefaultIcon;

// Small marker icon for polygon vertices
const VertexIcon = L.icon({
  iconUrl: icon,
  shadowUrl: iconShadow,
  iconSize: [15, 24],
  iconAnchor: [7, 24]
});

// Numbered marker icon for waypoints (green for user-drawn)
const createNumberedIcon = (number: number): L.DivIcon => {
  return L.divIcon({
    className: 'numbered-waypoint-icon',
    html: `<div class="waypoint-marker">${number}</div>`,
    iconSize: [32, 32],
    iconAnchor: [16, 32],
  });
};

// Numbered marker icon for generated waypoints (blue)
const createGeneratedWaypointIcon = (number: number): L.DivIcon => {
  return L.divIcon({
    className: 'numbered-waypoint-icon',
    html: `<div class="waypoint-marker generated">${number}</div>`,
    iconSize: [32, 32],
    iconAnchor: [16, 32],
  });
};

// Simple circle marker for search pattern waypoints (no number)
const SearchPatternWaypointIcon = L.divIcon({
  className: 'search-pattern-waypoint-icon',
  html: `<div class="search-waypoint-dot"></div>`,
  iconSize: [8, 8],
  iconAnchor: [4, 4],
});

// Platform marker with heading indication
const createPlatformIcon = (heading: number): L.DivIcon => {
  return L.divIcon({
    className: 'platform-marker-icon',
    html: `<div class="platform-marker" style="transform: rotate(${heading}deg)"><div class="platform-marker-icon">▲</div></div>`,
    iconSize: [32, 32],
    iconAnchor: [16, 16],
  });
};

/**
 * Component to handle map click events for drawing
 */
const DrawingHandler: React.FC = () => {
  const { drawingMode, setDrawnPolygon, setDrawnWaypoints, finishDrawing } = useMapContext();
  const [vertices, setVertices] = useState<Position[]>([]);
  const [waypoints, setWaypoints] = useState<Position[]>([]);

  // Reset state when drawing mode changes
  useEffect(() => {
    if (drawingMode === 'polygon') {
      setVertices([]);
    } else if (drawingMode === 'waypoint') {
      setWaypoints([]);
    }
  }, [drawingMode]);

  // Sync waypoints to context (add default speed)
  useEffect(() => {
    if (drawingMode === 'waypoint' && waypoints.length > 0) {
      const waypointsWithSpeed: Waypoint[] = waypoints.map(wp => ({
        ...wp,
        speed: 4.0, // Default speed 4.0 m/s
      }));
      setDrawnWaypoints(waypointsWithSpeed);
    }
  }, [waypoints, drawingMode, setDrawnWaypoints]);

  const map = useMapEvents({
    click: (e) => {
      if (drawingMode === 'polygon') {
        const newVertex: Position = {
          lat: e.latlng.lat,
          lon: e.latlng.lng,
        };

        // Check if clicking near first vertex to close polygon (pixel-based distance)
        if (vertices.length >= 3) {
          const firstVertex = vertices[0];
          const firstVertexPoint = map.latLngToContainerPoint([firstVertex.lat, firstVertex.lon]);
          const clickPoint = map.latLngToContainerPoint(e.latlng);

          const pixelDistance = Math.sqrt(
            Math.pow(clickPoint.x - firstVertexPoint.x, 2) +
            Math.pow(clickPoint.y - firstVertexPoint.y, 2)
          );

          // If within 15 pixels, close the polygon
          if (pixelDistance < 15) {
            setDrawnPolygon({ vertices: [...vertices] });
            finishDrawing();
            setVertices([]);
            return;
          }
        }

        setVertices([...vertices, newVertex]);
      } else if (drawingMode === 'waypoint') {
        const newWaypoint: Position = {
          lat: e.latlng.lat,
          lon: e.latlng.lng,
        };
        setWaypoints([...waypoints, newWaypoint]);
      }
    },
    dblclick: (e) => {
      if (drawingMode === 'polygon' && vertices.length >= 3) {
        // Double-click to finish polygon
        e.originalEvent.preventDefault();
        setDrawnPolygon({ vertices: [...vertices] });
        finishDrawing();
        setVertices([]);
      }
    },
  });

  // Render current drawing state
  return (
    <>
      {/* Polygon drawing mode */}
      {drawingMode === 'polygon' && (
        <>
          {/* Show vertices being drawn */}
          {vertices.map((vertex, index) => (
            <Marker
              key={`vertex-${index}`}
              position={[vertex.lat, vertex.lon]}
              icon={VertexIcon}
            />
          ))}

          {/* Show lines connecting vertices */}
          {vertices.length > 1 && (
            <Polyline
              positions={vertices.map(v => [v.lat, v.lon])}
              color="#2196F3"
              weight={2}
              dashArray="5, 5"
            />
          )}

          {/* Show line from last vertex to first (closing line hint) */}
          {vertices.length >= 3 && (
            <Polyline
              positions={[
                [vertices[vertices.length - 1].lat, vertices[vertices.length - 1].lon],
                [vertices[0].lat, vertices[0].lon]
              ]}
              color="#2196F3"
              weight={2}
              dashArray="10, 10"
              opacity={0.5}
            />
          )}
        </>
      )}

      {/* Waypoint drawing mode */}
      {drawingMode === 'waypoint' && (
        <>
          {/* Show numbered waypoint markers */}
          {waypoints.map((waypoint, index) => (
            <Marker
              key={`waypoint-${index}`}
              position={[waypoint.lat, waypoint.lon]}
              icon={createNumberedIcon(index + 1)}
            />
          ))}

          {/* Show lines connecting waypoints */}
          {waypoints.length > 1 && (
            <Polyline
              positions={waypoints.map(w => [w.lat, w.lon])}
              color="#4CAF50"
              weight={3}
              dashArray="5, 10"
            />
          )}
        </>
      )}
    </>
  );
};

interface MapPanelProps {
  behaviors: Behavior[];
}

/**
 * Map panel component with Leaflet.
 *
 * Displays OpenStreetMap centered on Portland Harbour (50.6°N, 2.4°W).
 * Zoom level 12 provides good overview of the operating area.
 * Supports interactive polygon drawing and waypoint placement.
 * Displays planned behaviors from mission plan.
 */
const MapPanel: React.FC<MapPanelProps> = ({ behaviors }) => {
  const { drawingMode, drawnPolygon, drawnWaypoints, generatedWaypoints, clearDrawing, finishDrawing } = useMapContext();
  const [simState, setSimState] = useState<SimulationState | null>(null);
  const [trackHistory, setTrackHistory] = useState<Position[]>([]);

  // Subscribe to simulation service for platform updates
  useEffect(() => {
    const simService = getSimulationService();
    let updateCount = 0;
    let lastLogTime = Date.now();

    const unsubscribe = simService.subscribe((state) => {
      setSimState(state);

      // Debug: Log position updates
      updateCount++;
      const now = Date.now();
      if (now - lastLogTime > 1000) {
        const pos = state.platformState?.position;
        console.log(`[MapPanel] Updates/sec: ${updateCount}, Position: lat=${pos?.lat.toFixed(6)}, lon=${pos?.lon.toFixed(6)}`);
        updateCount = 0;
        lastLogTime = now;
      }

      // Update track history (keep last 100 positions)
      if (state.running && state.platformState) {
        setTrackHistory((prev) => {
          const newHistory = [...prev, state.platformState.position];
          return newHistory.slice(-100); // Keep last 100 positions
        });
      }
    });

    // Initialize
    setSimState(simService.getState());

    return unsubscribe;
  }, []);

  // Clear track history when simulation stops
  useEffect(() => {
    if (simState && !simState.running) {
      setTrackHistory([]);
    }
  }, [simState]);

  // Portland Harbour coordinates
  const portlandHarbour: [number, number] = [50.6, -2.4];

  return (
    <div className="map-panel">
      <MapContainer
        center={portlandHarbour}
        zoom={12}
        style={{ height: '100%', width: '100%' }}
        doubleClickZoom={false} // Disable to allow double-click for polygon completion
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <Marker position={portlandHarbour}>
          <Popup>
            Portland Harbour<br />Operating Area
          </Popup>
        </Marker>

        {/* Drawing handler */}
        <DrawingHandler />

        {/* Display completed polygon */}
        {drawnPolygon && (
          <>
            <LeafletPolygon
              positions={drawnPolygon.vertices.map(v => [v.lat, v.lon])}
              color="#2196F3"
              fillColor="#2196F3"
              fillOpacity={0.2}
              weight={3}
            />
            {drawnPolygon.vertices.map((vertex, index) => (
              <Marker
                key={`drawn-vertex-${index}`}
                position={[vertex.lat, vertex.lon]}
                icon={VertexIcon}
              />
            ))}
          </>
        )}

        {/* Display completed waypoints */}
        {drawnWaypoints.length > 0 && (
          <>
            {drawnWaypoints.map((waypoint, index) => (
              <Marker
                key={`drawn-waypoint-${index}`}
                position={[waypoint.lat, waypoint.lon]}
                icon={createNumberedIcon(index + 1)}
              />
            ))}
            {drawnWaypoints.length > 1 && (
              <Polyline
                positions={drawnWaypoints.map(w => [w.lat, w.lon])}
                color="#4CAF50"
                weight={3}
              />
            )}
          </>
        )}

        {/* Display generated waypoints from pattern API */}
        {generatedWaypoints.length > 0 && (
          <>
            {generatedWaypoints.map((waypoint, index) => (
              <Marker
                key={`generated-waypoint-${index}`}
                position={[waypoint.lat, waypoint.lon]}
                icon={createGeneratedWaypointIcon(index + 1)}
              />
            ))}
            {generatedWaypoints.length > 1 && (
              <Polyline
                positions={generatedWaypoints.map(w => [w.lat, w.lon])}
                color="#2196F3"
                weight={3}
              />
            )}
          </>
        )}

        {/* Display planned behaviors from mission plan */}
        {behaviors.map((behavior) => {
          const config = behavior.config;

          // Render search pattern polygons (parallel-track, expanding-square)
          if ((config.type === 'parallel-track' || config.type === 'expanding-square') && config.polygon) {
            return (
              <React.Fragment key={`behavior-search-${behavior.id}`}>
                {/* Bounding polygon */}
                <LeafletPolygon
                  positions={config.polygon.vertices.map(v => [v.lat, v.lon])}
                  color="#FF6B6B"
                  fillColor="#FF6B6B"
                  fillOpacity={0.15}
                  weight={2}
                  dashArray="5, 10"
                />
                {/* Waypoints inside search pattern */}
                {config.waypoints && config.waypoints.length > 0 && (
                  <>
                    {config.waypoints.map((waypoint, wpIndex) => (
                      <Marker
                        key={`behavior-${behavior.id}-wp-${wpIndex}`}
                        position={[waypoint.lat, waypoint.lon]}
                        icon={SearchPatternWaypointIcon}
                      />
                    ))}
                    <Polyline
                      positions={config.waypoints.map(w => [w.lat, w.lon])}
                      color="#2196F3"
                      weight={2}
                    />
                  </>
                )}
              </React.Fragment>
            );
          }

          // Render waypoint transit
          if (config.type === 'waypoint-transit' && config.waypoints) {
            return (
              <React.Fragment key={`behavior-waypoints-${behavior.id}`}>
                {config.waypoints.map((waypoint, wpIndex) => (
                  <Marker
                    key={`behavior-${behavior.id}-wp-${wpIndex}`}
                    position={[waypoint.lat, waypoint.lon]}
                    icon={createNumberedIcon(wpIndex + 1)}
                  />
                ))}
                {config.waypoints.length > 1 && (
                  <Polyline
                    positions={config.waypoints.map(w => [w.lat, w.lon])}
                    color="#4CAF50"
                    weight={3}
                  />
                )}
              </React.Fragment>
            );
          }

          // Render return to base
          if (config.type === 'return-to-base' && config.basePosition) {
            return (
              <Marker
                key={`behavior-base-${behavior.id}`}
                position={[config.basePosition.lat, config.basePosition.lon]}
                icon={DefaultIcon}
              >
                <Popup>Return to Base</Popup>
              </Marker>
            );
          }

          return null;
        })}

        {/* Platform marker and track history */}
        {simState?.platformState && (
          <>
            {/* Track history */}
            {trackHistory.length > 1 && (
              <Polyline
                positions={trackHistory.map((pos) => [pos.lat, pos.lon])}
                color="#FF5722"
                weight={2}
                opacity={0.6}
                dashArray="5, 5"
              />
            )}

            {/* Platform marker */}
            <Marker
              key={`platform-${simState.platformState.position.lat}-${simState.platformState.position.lon}`}
              position={[simState.platformState.position.lat, simState.platformState.position.lon]}
              icon={createPlatformIcon(simState.platformState.heading)}
            >
              <Popup>
                <strong>Platform</strong>
                <br />
                Position: {simState.platformState.position.lat.toFixed(6)}°, {simState.platformState.position.lon.toFixed(6)}°
                <br />
                Heading: {simState.platformState.heading.toFixed(1)}°
                <br />
                Speed: {simState.platformState.speed.toFixed(2)} m/s ({(simState.platformState.speed * 1.94384).toFixed(2)} kn)
              </Popup>
            </Marker>
          </>
        )}
      </MapContainer>

      {/* Polygon drawing mode overlay */}
      {drawingMode === 'polygon' && (
        <div className="drawing-overlay">
          <div className="drawing-instructions">
            <strong>Drawing Polygon</strong>
            <p>Click to add vertices, double-click or click first vertex to finish</p>
            <button className="cancel-drawing-button" onClick={clearDrawing}>
              Cancel
            </button>
          </div>
        </div>
      )}

      {/* Waypoint drawing mode overlay */}
      {drawingMode === 'waypoint' && (
        <div className="drawing-overlay">
          <div className="drawing-instructions waypoint-instructions">
            <strong>Adding Waypoints</strong>
            <p>Click on the map to add waypoints in sequence</p>
            <div className="drawing-buttons">
              <button className="finish-drawing-button" onClick={finishDrawing}>
                Finish
              </button>
              <button className="cancel-drawing-button" onClick={clearDrawing}>
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MapPanel;
