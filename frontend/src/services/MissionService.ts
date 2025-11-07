import { Behavior } from '../types/behaviors';

export interface Mission {
  name: string;
  description?: string;
  createdAt: string;
  behaviors: Behavior[];
}

/**
 * Download mission as JSON file to user's local filesystem.
 * Uses browser's download mechanism (Blob + URL.createObjectURL).
 */
export const downloadMission = (behaviors: Behavior[], filename?: string): void => {
  const mission: Mission = {
    name: filename || `Mission ${new Date().toISOString().split('T')[0]}`,
    description: 'USV Mission Plan',
    createdAt: new Date().toISOString(),
    behaviors,
  };

  const jsonString = JSON.stringify(mission, null, 2);
  const blob = new Blob([jsonString], { type: 'application/json' });
  const url = URL.createObjectURL(blob);

  const link = document.createElement('a');
  link.href = url;
  link.download = (filename || `mission-${Date.now()}`) + '.json';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);

  // Clean up URL object
  URL.revokeObjectURL(url);
};

/**
 * Upload mission from JSON file (using FileReader).
 * Returns parsed mission with behaviors array.
 */
export const uploadMission = (file: File): Promise<Mission> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();

    reader.onload = (event) => {
      try {
        const content = event.target?.result as string;
        const mission: Mission = JSON.parse(content);

        // Validate mission structure
        if (!mission.behaviors || !Array.isArray(mission.behaviors)) {
          throw new Error('Invalid mission file: missing behaviors array');
        }

        resolve(mission);
      } catch (error: any) {
        reject(new Error(`Failed to parse mission file: ${error.message}`));
      }
    };

    reader.onerror = () => {
      reject(new Error('Failed to read mission file'));
    };

    reader.readAsText(file);
  });
};
