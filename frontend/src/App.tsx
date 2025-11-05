import { useState, useEffect } from 'react';
import './App.css';
import MapPanel from './components/MapPanel';
import MissionPlanPanel from './components/MissionPlanPanel';
import ControlPanel from './components/ControlPanel';
import StatePanel from './components/StatePanel';
import { checkHealth } from './services/HealthService';

interface HealthStatus {
  status: string;
  timestamp: string;
  version: string;
  service: string;
}

function App() {
  const [healthStatus, setHealthStatus] = useState<'up' | 'down' | 'checking'>('checking');
  const [showReconnectModal, setShowReconnectModal] = useState(false);

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

  return (
    <div className="app">
      <div className="app-left">
        <MapPanel />
        <div className="health-indicator">
          <div className={`health-dot ${healthStatus === 'up' ? 'up' : 'down'}`}></div>
          <span>{healthStatus === 'up' ? 'Connected' : 'Disconnected'}</span>
        </div>
      </div>
      <div className="app-right">
        <ControlPanel />
        <MissionPlanPanel />
        <StatePanel />
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
  );
}

export default App;
