import { useMemo } from 'react';

export const useCanvasConfig = (dimension, gridWidth) => {
  return useMemo(() => {
    const maxCanvasSize = Math.min(600, window.innerWidth * 0.8, window.innerHeight * 0.6);
    const cellSize = maxCanvasSize / dimension;
    const canvasSize = dimension * cellSize;
    const scaleFactor = cellSize / gridWidth; // Use actual gridWidth instead of hardcoded 200
    
    return { cellSize, canvasSize, scaleFactor };
  }, [dimension, gridWidth]);
};