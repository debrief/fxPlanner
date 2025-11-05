import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import './MapPanel.css';

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

/**
 * Map panel component with Leaflet.
 *
 * Displays OpenStreetMap centered on Portland Harbour (50.6°N, 2.4°W).
 * Zoom level 12 provides good overview of the operating area.
 */
const MapPanel: React.FC = () => {
  // Portland Harbour coordinates
  const portlandHarbour: [number, number] = [50.6, -2.4];

  return (
    <div className="map-panel">
      <MapContainer
        center={portlandHarbour}
        zoom={12}
        style={{ height: '100%', width: '100%' }}
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
      </MapContainer>
    </div>
  );
};

export default MapPanel;
