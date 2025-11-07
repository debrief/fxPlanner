import { useState, useEffect } from 'react';
import './App.css';
import MapPanel from './components/MapPanel';
import MissionPlanPanel from './components/MissionPlanPanel';
import ControlPanel from './components/ControlPanel';
import StatePanel from './components/StatePanel';
import TabbedPanel, { Tab } from './components/TabbedPanel';
import WaypointTransitPanel from './components/behaviors/WaypointTransitPanel';
import ParallelTrackSearchPanel from './components/behaviors/ParallelTrackSearchPanel';
import ExpandingSquareSearchPanel from './components/behaviors/ExpandingSquareSearchPanel';
import ReturnToBasePanel from './components/behaviors/ReturnToBasePanel';
import { Waypoint, Polygon, ParallelTrackParams, ExpandingSquareParams, Position, Behavior } from './types/behaviors';
import { checkHealth } from './services/HealthService';
import { MapProvider } from './contexts/MapContext';
import { getSimulationService } from './services/SimulationService';

interface HealthStatus {
  status: string;
  timestamp: string;
  version: string;
  service: string;
}

function App() {
  const [healthStatus, setHealthStatus] = useState<'up' | 'down' | 'checking'>('checking');
  const [showReconnectModal, setShowReconnectModal] = useState(false);
  const [behaviors, setBehaviors] = useState<Behavior[]>([]);
  const [tabs, setTabs] = useState<Tab[]>([
    {
      id: 'platform-state',
      label: 'Platform State',
      content: <StatePanel />,
      closeable: false,
    }
  ]);
  const [activeTabId, setActiveTabId] = useState<string>('platform-state');

  useEffect(() => {
    // Health check every 5 seconds
    const checkHealthStatus = async () => {
      try {
        const response: HealthStatus = await checkHealth();
        if (response.status === 'UP') {
          setHealthStatus('up');
          setShowReconnectModal(false);
        } else {
          setHealthStatus('down');
          setShowReconnectModal(true);
        }
      } catch (error) {
        setHealthStatus('down');
        setShowReconnectModal(true);
      }
    };

    // Initial check
    checkHealthStatus();

    // Set up interval
    const interval = setInterval(checkHealthStatus, 5000);

    return () => clearInterval(interval);
  }, []);

  // Tab management functions
  const handleAddBehavior = (type: string) => {
    console.log('Adding behavior tab:', type);

    let newTab: Tab;
    let tabId: string;

    switch (type) {
      case 'waypoint-transit':
        tabId = `waypoint-transit-${Date.now()}`;
        newTab = {
          id: tabId,
          label: 'Waypoint Transit',
          content: (
            <WaypointTransitPanel
              onConfirm={(waypoints) => handleWaypointTransitConfirm(tabId, waypoints)}
              onCancel={() => handleTabClose(tabId)}
            />
          ),
          closeable: true,
        };
        break;

      case 'parallel-track':
        tabId = `parallel-track-${Date.now()}`;
        newTab = {
          id: tabId,
          label: 'Parallel Track',
          content: (
            <ParallelTrackSearchPanel
              onConfirm={(polygon, params, waypoints, estimatedDuration) => handleParallelTrackConfirm(tabId, polygon, params, waypoints, estimatedDuration)}
              onCancel={() => handleTabClose(tabId)}
            />
          ),
          closeable: true,
        };
        break;

      case 'expanding-square':
        tabId = `expanding-square-${Date.now()}`;
        newTab = {
          id: tabId,
          label: 'Expanding Square',
          content: (
            <ExpandingSquareSearchPanel
              onConfirm={(polygon, params, waypoints, estimatedDuration) => handleExpandingSquareConfirm(tabId, polygon, params, waypoints, estimatedDuration)}
              onCancel={() => handleTabClose(tabId)}
            />
          ),
          closeable: true,
        };
        break;

      case 'return-to-base':
        tabId = `return-to-base-${Date.now()}`;
        newTab = {
          id: tabId,
          label: 'Return to Base',
          content: (
            <ReturnToBasePanel
              onConfirm={(basePosition, platformSpeed) => handleReturnToBaseConfirm(tabId, basePosition, platformSpeed)}
              onCancel={() => handleTabClose(tabId)}
            />
          ),
          closeable: true,
        };
        break;

      default:
        console.warn('Unsupported behavior type:', type);
        return;
    }

    // Add tab and set as active
    setTabs(prev => [...prev, newTab]);
    setActiveTabId(tabId);
  };

  const handleTabChange = (tabId: string) => {
    setActiveTabId(tabId);
  };

  const handleTabClose = (tabId: string) => {
    // Remove tab
    setTabs(prev => prev.filter(tab => tab.id !== tabId));

    // If active tab was closed, switch to platform state
    if (activeTabId === tabId) {
      setActiveTabId('platform-state');
    }
  };

  const handleWaypointTransitConfirm = (tabId: string, waypoints: Waypoint[]) => {
    console.log('Waypoint Transit configured:', waypoints);

    // Create behavior object
    const newBehavior: Behavior = {
      id: `behavior-${Date.now()}`,
      name: `Waypoint Transit (${waypoints.length} waypoints)`,
      config: {
        type: 'waypoint-transit',
        waypoints,
      },
      status: 'PENDING',
      estimatedWaypoints: waypoints.length,
    };

    // Add to mission plan
    setBehaviors(prev => [...prev, newBehavior]);

    // Close tab
    handleTabClose(tabId);
  };

  const handleParallelTrackConfirm = (tabId: string, polygon: Polygon, params: ParallelTrackParams, waypoints: Waypoint[], estimatedDuration: number) => {
    console.log('Parallel Track Search configured:', polygon, params);

    // Create behavior object
    const newBehavior: Behavior = {
      id: `behavior-${Date.now()}`,
      name: `Parallel Track Search (${waypoints.length} waypoints)`,
      config: {
        type: 'parallel-track',
        polygon,
        params,
        waypoints, // Store generated waypoints
      },
      status: 'PENDING',
      estimatedWaypoints: waypoints.length,
      estimatedDuration,
    };

    // Add to mission plan
    setBehaviors(prev => [...prev, newBehavior]);

    // Close tab
    handleTabClose(tabId);
  };

  const handleExpandingSquareConfirm = (tabId: string, polygon: Polygon, params: ExpandingSquareParams, waypoints: Waypoint[], estimatedDuration: number) => {
    console.log('Expanding Square Search configured:', polygon, params);

    // Create behavior object
    const newBehavior: Behavior = {
      id: `behavior-${Date.now()}`,
      name: `Expanding Square Search (${waypoints.length} waypoints)`,
      config: {
        type: 'expanding-square',
        polygon,
        params,
        waypoints, // Store generated waypoints
      },
      status: 'PENDING',
      estimatedWaypoints: waypoints.length,
      estimatedDuration,
    };

    // Add to mission plan
    setBehaviors(prev => [...prev, newBehavior]);

    // Close tab
    handleTabClose(tabId);
  };

  const handleReturnToBaseConfirm = (tabId: string, basePosition: Position, platformSpeed: number) => {
    console.log('Return to Base configured:', basePosition, platformSpeed);

    // Create behavior object
    const newBehavior: Behavior = {
      id: `behavior-${Date.now()}`,
      name: `Return to Base (${basePosition.lat.toFixed(2)}°, ${basePosition.lon.toFixed(2)}°)`,
      config: {
        type: 'return-to-base',
        basePosition,
        platformSpeed,
      },
      status: 'PENDING',
      estimatedWaypoints: 1,
    };

    // Add to mission plan
    setBehaviors(prev => [...prev, newBehavior]);

    // Close tab
    handleTabClose(tabId);
  };

  const handleDeleteBehavior = (behaviorId: string) => {
    setBehaviors(prev => prev.filter(b => b.id !== behaviorId));
  };

  const handleLoadMission = (loadedBehaviors: Behavior[]) => {
    setBehaviors(loadedBehaviors);
  };

  // Load behaviors into simulation service whenever they change
  useEffect(() => {
    if (behaviors.length > 0) {
      getSimulationService().loadMission(behaviors);
    }
  }, [behaviors]);

  const handleMoveBehavior = (behaviorId: string, direction: 'up' | 'down') => {
    setBehaviors(prev => {
      const index = prev.findIndex(b => b.id === behaviorId);
      if (index === -1) return prev;

      // Can't move up if already first
      if (direction === 'up' && index === 0) return prev;
      // Can't move down if already last
      if (direction === 'down' && index === prev.length - 1) return prev;

      const newBehaviors = [...prev];
      const targetIndex = direction === 'up' ? index - 1 : index + 1;

      // Swap
      [newBehaviors[index], newBehaviors[targetIndex]] = [newBehaviors[targetIndex], newBehaviors[index]];

      return newBehaviors;
    });
  };

  return (
    <MapProvider>
      <div className="app">
        <div className="app-left">
          <MapPanel behaviors={behaviors} />
          <div className="health-indicator">
            <div className={`health-dot ${healthStatus === 'up' ? 'up' : 'down'}`}></div>
            <span>{healthStatus === 'up' ? 'Connected' : 'Disconnected'}</span>
          </div>
        </div>
        <div className="app-right">
          <ControlPanel hasBehaviors={behaviors.length > 0} />
          <MissionPlanPanel
            behaviors={behaviors}
            onAddBehavior={handleAddBehavior}
            onDeleteBehavior={handleDeleteBehavior}
            onMoveBehavior={handleMoveBehavior}
            onLoadMission={handleLoadMission}
          />
          <TabbedPanel
            tabs={tabs}
            activeTabId={activeTabId}
            onTabChange={handleTabChange}
            onTabClose={handleTabClose}
          />
        </div>

        {showReconnectModal && (
          <>
            <div className="modal-backdrop"></div>
            <div className="reconnecting-modal">
              <h3>Connection Lost</h3>
              <p>Reconnecting to server...</p>
            </div>
          </>
        )}
      </div>
    </MapProvider>
  );
}

export default App;
