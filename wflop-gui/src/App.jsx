import React, { useState } from 'react';
import LayoutForm from './components/LayoutForm';
import TurbineCanvas from './components/TurbineCanvas';

const WindProfileSelector = ({ windProfiles, selectedIndex, onSelectionChange }) => {
  return (
    <div style={{ 
      margin: '10px 0', 
      display: 'flex', 
      alignItems: 'center', 
      gap: '10px' 
    }}>
      <label htmlFor="wind-select">Wind Profile:</label>
      <select 
        id="wind-select"
        value={selectedIndex} 
        onChange={(e) => onSelectionChange(parseInt(e.target.value))}
        style={{ 
          padding: '5px 10px', 
          borderRadius: '4px', 
          border: '1px solid #ccc' 
        }}
      >
        {windProfiles.map((wind, index) => (
          <option key={index} value={index}>
            Wind {index + 1}: {wind.speed} m/s @ {wind.angle}°
          </option>
        ))}
      </select>
    </div>
  );
};

function App() {
  const [data, setData] = useState(null);
  const [updateKey, setUpdateKey] = useState(0);
  const [selectedWindIndex, setSelectedWindIndex] = useState(0);

  const handleResult = (result) => {
    setData(result);
    setUpdateKey(prev => prev + 1);
  };

  const handleLayoutUpdate = (updatedData) => {
    setData(updatedData);
    setUpdateKey(prev => prev + 1);
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
      <h1>WFLOP Visualizer</h1>
      <LayoutForm onResult={setData} />
      
      {data && data.solution && data.problem && (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '20px',
            marginBottom: '10px',
            padding: '10px 20px',
            backgroundColor: '#f8f9fa',
            borderRadius: '8px',
            border: '1px solid #dee2e6'
          }}>
            <p style={{ margin: 0, fontSize: '16px', fontWeight: 'bold' }}>
              Fitness: <span style={{ color: '#28a745' }}>
                {data.solution.fitness ? data.solution.fitness.toFixed(2) : 'N/A'}
              </span>
            </p>
            <p style={{ margin: 0, fontSize: '14px', color: '#6c757d' }}>
              Turbines: {
                data.solution.layout && data.solution.layout.turbineIndices 
                  ? data.solution.layout.turbineIndices.length 
                  : data.solution.layout 
                    ? data.solution.layout.length 
                    : 0
              }
            </p>
          </div>
          
          {data.problem.windProfiles && (
            <WindProfileSelector
              windProfiles={data.problem.windProfiles}
              selectedIndex={selectedWindIndex}
              onSelectionChange={setSelectedWindIndex}
            />
          )}

          <TurbineCanvas 
            key={updateKey}
            layout={data.solution.layout || { turbineIndices: data.solution.layout || [] }}
            dimension={data.problem.dimension}
            gridWidth={data.problem.gridWidth}
            selectedWind={data.problem.windProfiles ? data.problem.windProfiles[selectedWindIndex] : null}
            rotorRadius={data.problem.rotorRadius}
            hubHeight={data.problem.hubHeight}
            surfaceRoughness={data.problem.surfaceRoughness}
            problem={data.problem}
            onLayoutUpdate={handleLayoutUpdate}
          />
        </div>
      )}
    </div>
  );
}

export default App;