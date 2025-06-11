import React, { useCallback } from 'react';
import { Graphics } from '@pixi/react';

// This component is now unused since we moved the wind indicator to the side panel
// Keeping it for backward compatibility but it won't render anything
export const WindIndicator = ({ selectedWind, canvasSize }) => {
  const drawEmpty = useCallback((g) => {
    g.clear();
    // Don't draw anything - wind indicator is now in the side panel
  }, []);

  return <Graphics draw={drawEmpty} />;
};