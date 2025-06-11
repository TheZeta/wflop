import React, { useCallback, useEffect } from 'react';
import { Graphics } from '@pixi/react';

export const InteractiveGrid = ({ 
  dimension, 
  cellSize, 
  canvasSize, 
  currentLayout, 
  isEvaluating, 
  onCellClick 
}) => {
  useEffect(() => {
    console.log('🔧 InteractiveGrid mounted/updated:', {
      dimension,
      cellSize,
      canvasSize,
      hasOnCellClick: typeof onCellClick === 'function',
      currentLayout: currentLayout?.turbineIndices,
      isEvaluating
    });
  }, [dimension, cellSize, canvasSize, onCellClick, currentLayout, isEvaluating]);

  const drawInteractiveGrid = useCallback((g) => {
    console.log('🎨 Drawing interactive grid...');
    
    g.clear();
    
    // Set up interactivity with more explicit settings
    g.eventMode = 'static';
    g.interactive = true;
    g.interactiveChildren = false; // Prevent child elements from blocking
    g.cursor = isEvaluating ? 'wait' : 'pointer';
    
    console.log('🖱️ Grid eventMode:', g.eventMode, 'interactive:', g.interactive);
    
    // Draw grid lines
    g.lineStyle(1, 0xcccccc, 0.8);
    for (let i = 0; i <= dimension; i++) {
      const pos = i * cellSize;
      g.moveTo(pos, 0);
      g.lineTo(pos, canvasSize);
      g.moveTo(0, pos);
      g.lineTo(canvasSize, pos);
    }

    // Draw subtle highlights for cells with turbines
    for (let row = 0; row < dimension; row++) {
      for (let col = 0; col < dimension; col++) {
        const cellIndex = row * dimension + col;
        const x = col * cellSize;
        const y = row * cellSize;
        
        const hasTurbine = currentLayout?.turbineIndices?.includes(cellIndex) || false;
        
        if (hasTurbine) {
          g.beginFill(0x90EE90, 0.15);
          g.drawRect(x, y, cellSize, cellSize);
          g.endFill();
        }
      }
    }
    
    // Create a fully transparent but clickable overlay
    g.beginFill(0x000000, 0);
    g.drawRect(0, 0, canvasSize, canvasSize);
    g.endFill();
    
    // Remove existing listeners
    g.removeAllListeners();
    
    // Simple click handler
    const handlePointerDown = (event) => {
      console.log('🖱️ POINTER DOWN EVENT FIRED!');
      
      if (isEvaluating) {
        console.log('⏳ Evaluation in progress, ignoring click');
        return;
      }
      
      // Stop event propagation to prevent bubbling
      event.stopPropagation();
      
      const bounds = g.getBounds();
      const localPoint = event.data.getLocalPosition(g);
      
      console.log('📍 Bounds:', bounds);
      console.log('📍 Local point:', localPoint);
      console.log('📍 Global point:', event.data.global);
      
      const col = Math.floor(localPoint.x / cellSize);
      const row = Math.floor(localPoint.y / cellSize);
      
      console.log('📍 Grid position:', { col, row, cellSize, dimension });
      
      if (col >= 0 && col < dimension && row >= 0 && row < dimension) {
        const cellIndex = row * dimension + col;
        console.log('🎯 Cell index calculated:', cellIndex);
        
        if (typeof onCellClick === 'function') {
          console.log('✅ Calling onCellClick with index:', cellIndex);
          onCellClick(cellIndex);
        } else {
          console.error('❌ onCellClick is not a function!', typeof onCellClick);
        }
      } else {
        console.log('❌ Click outside bounds:', { col, row, dimension });
      }
    };
    
    // Use only pointerdown for more reliable detection
    g.on('pointerdown', handlePointerDown);
    
    // Add visual feedback
    g.on('pointerover', () => {
      console.log('🖱️ Mouse OVER grid');
    });
    
    g.on('pointerout', () => {
      console.log('🖱️ Mouse OUT grid');
    });
    
    console.log('✅ Grid drawing complete, pointerdown event attached');
    
  }, [dimension, cellSize, canvasSize, currentLayout, onCellClick, isEvaluating]);

  return <Graphics draw={drawInteractiveGrid} />;
};