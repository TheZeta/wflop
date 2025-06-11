import React, { useState, useCallback, useEffect } from 'react';
import { Stage, Container } from '@pixi/react';
import { getTurbineCoordinates } from '../utils/layoutUtils';
import { evaluateLayout } from '../services/layoutService';
import { InteractiveGrid } from './canvas/InteractiveGrid';
import { InteractiveOverlay } from './canvas/InteractiveOverlay';
import { WakeCones } from './canvas/WakeCones';
import { WindIndicator } from './canvas/WindIndicator';
import { TurbineRenderer } from './canvas/TurbineRenderer';
import { WindInfoPanel } from './canvas/WindInfoPanel';
import { CanvasLegend } from './canvas/CanvasLegend';
import { useCanvasConfig } from '../hooks/useCanvasConfig';

const TurbineCanvas = ({ 
  layout, 
  dimension, 
  gridWidth, 
  selectedWind, 
  rotorRadius, 
  hubHeight, 
  surfaceRoughness,
  problem,
  onLayoutUpdate
}) => {
  const [isEvaluating, setIsEvaluating] = useState(false);
  
  // Add defensive checks and ensure we have a valid layout
  const safeLayout = layout || { turbineIndices: [] };
  const safeTurbineIndices = safeLayout.turbineIndices || safeLayout || [];
  
  const [currentLayout, setCurrentLayout] = useState({ 
    turbineIndices: Array.isArray(safeTurbineIndices) ? safeTurbineIndices : []
  });

  // 🔥 KEY FIX: Sync currentLayout with incoming layout prop
  useEffect(() => {
    const newSafeTurbineIndices = safeLayout.turbineIndices || safeLayout || [];
    const newLayout = { 
      turbineIndices: Array.isArray(newSafeTurbineIndices) ? newSafeTurbineIndices : []
    };
    
    console.log('🔄 Layout prop changed, updating currentLayout:', {
      oldLayout: currentLayout,
      newLayout: newLayout
    });
    
    setCurrentLayout(newLayout);
  }, [layout]); // Re-run when layout prop changes
  
  const canvasConfig = useCanvasConfig(dimension, gridWidth);
  const { cellSize, canvasSize, scaleFactor } = canvasConfig;
  
  // Force integer canvas size to avoid sub-pixel issues
  const intCanvasSize = Math.floor(canvasSize);
  const exactCellSize = intCanvasSize / dimension;
  
  console.log('📏 Canvas Config:', { 
    dimension, 
    gridWidth, 
    originalCanvasSize: canvasSize,
    intCanvasSize,
    exactCellSize,
    scaleFactor 
  });
  
  console.log('🎯 Layout Debug:', {
    layout,
    safeLayout,
    safeTurbineIndices,
    currentLayout,
    'currentLayout.turbineIndices': currentLayout.turbineIndices
  });
  
  // Shared coordinate calculation function - this ensures exact same logic
  const getCellCenter = (row, col) => {
    const x = Math.floor(col * exactCellSize + exactCellSize / 2);
    const y = Math.floor(row * exactCellSize + exactCellSize / 2);
    return { x, y };
  };
  
  // Calculate turbine coordinates using shared function - with safety check
  const canvasTurbines = (currentLayout.turbineIndices || []).map((index) => {
    const row = Math.floor(index / dimension);
    const col = index % dimension;
    const { x, y } = getCellCenter(row, col);
    
    console.log(`🎯 Turbine ${index}: row=${row}, col=${col}, x=${x}, y=${y}`);
    
    return { x, y };
  });

  // For wake cones, we still need the original coordinates scaled
  const originalTurbines = getTurbineCoordinates(currentLayout, dimension, gridWidth);
  const scaledTurbinesForWakes = originalTurbines.map(({ x, y }) => ({
    x: x * scaleFactor,
    y: y * scaleFactor
  }));

  // Calculate wake parameters
  const entrainmentConstant = 0.5 / Math.log(hubHeight / surfaceRoughness);

  const handleCellClick = useCallback(async (cellIndex) => {
    console.log('🚀 handleCellClick called with:', cellIndex);
    
    if (isEvaluating) {
      console.log('⏳ Already evaluating, returning early');
      return;
    }
    
    setIsEvaluating(true);

    try {
      const newTurbineIndices = [...(currentLayout.turbineIndices || [])];
      const turbineIndex = newTurbineIndices.indexOf(cellIndex);

      if (turbineIndex > -1) {
        newTurbineIndices.splice(turbineIndex, 1);
        console.log('🗑️ Removed turbine at index:', cellIndex);
      } else {
        newTurbineIndices.push(cellIndex);
        newTurbineIndices.sort((a, b) => a - b);
        console.log('➕ Added turbine at index:', cellIndex);
      }

      console.log('📋 New turbine indices:', newTurbineIndices);

      const updatedProblem = {
        ...problem,
        numberOfTurbines: newTurbineIndices.length
      };

      console.log('🚀 Evaluating layout...');
      const response = await evaluateLayout(updatedProblem, newTurbineIndices);
      console.log('✅ Evaluation response:', response);

      const newLayout = { turbineIndices: newTurbineIndices };
      setCurrentLayout(newLayout);

      onLayoutUpdate({
        solution: {
          layout: { turbineIndices: response.layout },
          fitness: response.fitness
        },
        problem: updatedProblem
      });

    } catch (error) {
      console.error('❌ Failed to evaluate layout:', error);
      alert(`Failed to evaluate new layout: ${error.message}`);
    } finally {
      setIsEvaluating(false);
    }
  }, [currentLayout, problem, onLayoutUpdate, isEvaluating]);

  // Test function to verify handler works
  const testClick = () => {
    console.log('🧪 Test button clicked');
    handleCellClick(0); // Test with cell index 0
  };

  return (
    <div style={{ 
      display: 'flex', 
      gap: '20px', 
      maxWidth: '100vw', 
      maxHeight: '90vh',
      padding: '10px'
    }}>
      {/* Left Panel - Controls and Info */}
      <div style={{ 
        flex: '0 0 300px',
        display: 'flex',
        flexDirection: 'column',
        gap: '15px'
      }}>
        <WindInfoPanel selectedWind={selectedWind} isEvaluating={isEvaluating} />
        <CanvasLegend />
        
        {/* Debug info */}
        <div style={{
          padding: '8px',
          backgroundColor: '#f0f0f0',
          borderRadius: '4px',
          fontSize: '12px'
        }}>
          <div>Dimension: {dimension}</div>
          <div>Canvas Size: {intCanvasSize}</div>
          <div>Cell Size: {exactCellSize.toFixed(2)}</div>
          <div>Scale Factor: {scaleFactor.toFixed(3)}</div>
          <div>Turbines: {currentLayout.turbineIndices ? currentLayout.turbineIndices.length : 0}</div>
        </div>
        
        {/* Debug button */}
        <button 
          onClick={testClick}
          style={{
            padding: '8px 16px',
            backgroundColor: '#007bff',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
            fontSize: '12px'
          }}
        >
          🧪 Test Click Handler (Cell 0)
        </button>
      </div>
      
      {/* Right Panel - Canvas */}
      <div style={{ 
        flex: '1',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center'
      }}>
        <div style={{ 
          border: '1px solid #ddd',
          borderRadius: '8px',
          padding: '0px',
          backgroundColor: '#f9f9f9',
          opacity: isEvaluating ? 0.7 : 1,
          transition: 'opacity 0.3s ease',
          position: 'relative'
        }}>
          {/* PIXI Canvas */}
          <Stage 
            width={intCanvasSize} 
            height={intCanvasSize} 
            options={{ 
              backgroundColor: 0xf5f5f5,
              antialias: true
            }}
            style={{
              display: 'block',
              margin: 0,
              padding: 0
            }}
          >
            <Container>
              {/* Background elements */}
              <WakeCones
                turbines={scaledTurbinesForWakes}
                selectedWind={selectedWind}
                rotorRadius={rotorRadius}
                entrainmentConstant={entrainmentConstant}
                scaleFactor={scaleFactor}
                canvasSize={intCanvasSize}
              />
              <TurbineRenderer turbines={canvasTurbines} cellSize={exactCellSize} />
            </Container>
          </Stage>
          
          {/* HTML Overlay for Click Detection */}
          <InteractiveOverlay
            dimension={dimension}
            cellSize={exactCellSize}
            canvasSize={intCanvasSize}
            currentLayout={currentLayout}
            isEvaluating={isEvaluating}
            onCellClick={handleCellClick}
            getCellCenter={getCellCenter}
          />
        </div>
      </div>
    </div>
  );
};

export default TurbineCanvas;