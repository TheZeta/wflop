import React, { useCallback } from 'react';
import { Graphics } from '@pixi/react';

const TurbineGraphics = ({ x, y, cellSize, index }) => {
  const drawTurbine = useCallback((g) => {
    g.clear();
    
    // Make sure this component doesn't interfere with mouse events
    g.eventMode = 'none';
    g.interactive = false;
    
    const turbineSize = cellSize * 0.6;
    const towerHeight = turbineSize * 0.7;
    const towerWidth = turbineSize * 0.08;
    const hubRadius = turbineSize * 0.12;
    const bladeLength = turbineSize * 0.4;
    
    // Position hub slightly above the center point
    const hubX = x;
    const hubY = y - towerHeight * 0.2;
    
    // Tower (extending downward from center)
    g.lineStyle(0);
    g.beginFill(0x555555);
    g.drawRect(hubX - towerWidth/2, hubY, towerWidth, towerHeight);
    g.endFill();
    
    // Hub
    g.beginFill(0x222222);
    g.drawCircle(hubX, hubY, hubRadius);
    g.endFill();
    
    // Blades
    g.lineStyle(2, 0x111111);
    g.beginFill(0xf8f8f8);
    
    for (let i = 0; i < 3; i++) {
      const angle = (i * 120) * Math.PI / 180;
      const bladeEndX = hubX + Math.cos(angle) * bladeLength;
      const bladeEndY = hubY + Math.sin(angle) * bladeLength;
      
      const bladeWidth = bladeLength * 0.2;
      const perpAngle = angle + Math.PI / 2;
      const halfWidth = bladeWidth / 2;
      
      const p1x = hubX + Math.cos(perpAngle) * halfWidth;
      const p1y = hubY + Math.sin(perpAngle) * halfWidth;
      const p2x = hubX - Math.cos(perpAngle) * halfWidth;
      const p2y = hubY - Math.sin(perpAngle) * halfWidth;
      const p3x = bladeEndX - Math.cos(perpAngle) * (halfWidth * 0.3);
      const p3y = bladeEndY - Math.sin(perpAngle) * (halfWidth * 0.3);
      const p4x = bladeEndX + Math.cos(perpAngle) * (halfWidth * 0.3);
      const p4y = bladeEndY + Math.sin(perpAngle) * (halfWidth * 0.3);
      
      g.moveTo(p1x, p1y);
      g.lineTo(p4x, p4y);
      g.lineTo(p3x, p3y);
      g.lineTo(p2x, p2y);
      g.lineTo(p1x, p1y);
    }
    g.endFill();
    
    // Hub overlay
    g.lineStyle(0);
    g.beginFill(0x222222);
    g.drawCircle(hubX, hubY, hubRadius);
    g.endFill();
    
  }, [x, y, cellSize, index]);

  return <Graphics draw={drawTurbine} />;
};

export const TurbineRenderer = ({ turbines, cellSize }) => {
  return (
    <>
      {turbines.map(({ x, y }, idx) => (
        <TurbineGraphics key={idx} x={x} y={y} cellSize={cellSize} index={idx} />
      ))}
    </>
  );
};