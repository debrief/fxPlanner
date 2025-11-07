import React, { createContext, useContext, useState, useCallback, ReactNode } from 'react';
import { Polygon, Waypoint } from '../types/behaviors';

/**
 * Drawing mode types for map interactions
 */
export type DrawingMode = 'none' | 'polygon' | 'waypoint';

/**
 * Map context state interface
 */
interface MapContextState {
  drawingMode: DrawingMode;
  drawnPolygon: Polygon | null;
  drawnWaypoints: Waypoint[];
  generatedWaypoints: Waypoint[];
  startPolygonDrawing: () => void;
  startWaypointDrawing: () => void;
  clearDrawing: () => void;
  finishDrawing: () => void;
  setDrawnPolygon: (polygon: Polygon | null) => void;
  setDrawnWaypoints: (waypoints: Waypoint[]) => void;
  setGeneratedWaypoints: (waypoints: Waypoint[]) => void;
}

/**
 * Default context value (throws error if used without provider)
 */
const MapContext = createContext<MapContextState | undefined>(undefined);

/**
 * Map context provider props
 */
interface MapProviderProps {
  children: ReactNode;
}

/**
 * Map context provider component.
 *
 * Manages shared map state between MapPanel and behavior dialogs.
 * Coordinates drawing mode, polygon creation, and waypoint placement.
 */
export const MapProvider: React.FC<MapProviderProps> = ({ children }) => {
  const [drawingMode, setDrawingMode] = useState<DrawingMode>('none');
  const [drawnPolygon, setDrawnPolygon] = useState<Polygon | null>(null);
  const [drawnWaypoints, setDrawnWaypoints] = useState<Waypoint[]>([]);
  const [generatedWaypoints, setGeneratedWaypoints] = useState<Waypoint[]>([]);

  /**
   * Activate polygon drawing mode
   */
  const startPolygonDrawing = useCallback(() => {
    setDrawingMode('polygon');
    setDrawnPolygon(null); // Clear any existing polygon
    setGeneratedWaypoints([]); // Clear generated waypoints
  }, []);

  /**
   * Activate waypoint drawing mode
   */
  const startWaypointDrawing = useCallback(() => {
    setDrawingMode('waypoint');
    setDrawnWaypoints([]); // Clear any existing waypoints
    setGeneratedWaypoints([]); // Clear generated waypoints
  }, []);

  /**
   * Clear all drawings and exit drawing mode
   */
  const clearDrawing = useCallback(() => {
    setDrawingMode('none');
    setDrawnPolygon(null);
    setDrawnWaypoints([]);
    setGeneratedWaypoints([]);
  }, []);

  /**
   * Exit drawing mode but preserve drawn elements
   */
  const finishDrawing = useCallback(() => {
    setDrawingMode('none');
  }, []);

  const value: MapContextState = {
    drawingMode,
    drawnPolygon,
    drawnWaypoints,
    generatedWaypoints,
    startPolygonDrawing,
    startWaypointDrawing,
    clearDrawing,
    finishDrawing,
    setDrawnPolygon,
    setDrawnWaypoints,
    setGeneratedWaypoints,
  };

  return <MapContext.Provider value={value}>{children}</MapContext.Provider>;
};

/**
 * Hook to access map context.
 *
 * @throws Error if used outside MapProvider
 */
export const useMapContext = (): MapContextState => {
  const context = useContext(MapContext);
  if (context === undefined) {
    throw new Error('useMapContext must be used within a MapProvider');
  }
  return context;
};
