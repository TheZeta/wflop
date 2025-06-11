import React, { useCallback } from 'react';
import { Graphics } from '@pixi/react';

export const WakeCones = ({ 
  turbines, 
  selectedWind, 
  rotorRadius, 
  entrainmentConstant, 
  scaleFactor, 
  canvasSize 
}) => {
  const drawWakeCones = useCallback((g) => {
    g.clear();
    
    // Make sure this component doesn't interfere with mouse events
    g.eventMode = 'none';
    g.interactive = false;
    
    // More visible wake cones
    g.lineStyle(2, 0xff6b6b, 0.9);
    g.beginFill(0xff6b6b, 0.2);

    const maxWakeDistance = canvasSize * 1.2;
    const scaledRotorRadius = rotorRadius * scaleFactor;
    const mathAngle = (90 - selectedWind.angle) * Math.PI / 180;

    turbines.forEach(({ x, y }) => {
      const dx = Math.cos(mathAngle);
      const dy = Math.sin(mathAngle);
      
      const wakeLength = maxWakeDistance;
      const endX = x + dx * wakeLength;
      const endY = y + dy * wakeLength;
      
      const scaledEntrainmentConstant = entrainmentConstant * scaleFactor;
      const finalRadius = scaledRotorRadius + scaledEntrainmentConstant * wakeLength;
      
      const perpX = -dy;
      const perpY = dx;
      
      g.moveTo(x + perpX * scaledRotorRadius, y + perpY * scaledRotorRadius);
      g.lineTo(endX + perpX * finalRadius, endY + perpY * finalRadius);
      g.lineTo(endX - perpX * finalRadius, endY - perpY * finalRadius);
      g.lineTo(x - perpX * scaledRotorRadius, y - perpY * scaledRotorRadius);
      g.lineTo(x + perpX * scaledRotorRadius, y + perpY * scaledRotorRadius);
    });
    
    g.endFill();
  }, [turbines, selectedWind.angle, rotorRadius, entrainmentConstant, scaleFactor, canvasSize]);

  return <Graphics draw={drawWakeCones} />;
};