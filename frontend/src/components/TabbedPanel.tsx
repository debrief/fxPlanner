import { ReactNode } from 'react';
import './TabbedPanel.css';

export interface Tab {
  id: string;
  label: string;
  content: ReactNode;
  closeable: boolean;
}

interface TabbedPanelProps {
  tabs: Tab[];
  activeTabId: string;
  onTabChange: (tabId: string) => void;
  onTabClose: (tabId: string) => void;
}

/**
 * Tabbed panel component.
 *
 * Manages tabs for platform state and dynamic behavior configuration.
 * Default "Platform State" tab is always present and not closeable.
 * Behavior configuration tabs are dynamically added and closeable.
 */
const TabbedPanel: React.FC<TabbedPanelProps> = ({
  tabs,
  activeTabId,
  onTabChange,
  onTabClose,
}) => {
  const activeTab = tabs.find(tab => tab.id === activeTabId);

  const handleTabClick = (tabId: string) => {
    onTabChange(tabId);
  };

  const handleTabClose = (e: React.MouseEvent, tabId: string) => {
    e.stopPropagation(); // Prevent tab selection when closing
    onTabClose(tabId);
  };

  return (
    <div className="tabbed-panel">
      <div className="tab-header-bar">
        {tabs.map(tab => (
          <div
            key={tab.id}
            className={`tab-header ${tab.id === activeTabId ? 'active' : ''}`}
            onClick={() => handleTabClick(tab.id)}
          >
            <span className="tab-label">{tab.label}</span>
            {tab.closeable && (
              <button
                className="tab-close-button"
                onClick={(e) => handleTabClose(e, tab.id)}
                aria-label="Close tab"
              >
                ×
              </button>
            )}
          </div>
        ))}
      </div>
      <div className="tab-content-area">
        {activeTab ? activeTab.content : null}
      </div>
    </div>
  );
};

export default TabbedPanel;
