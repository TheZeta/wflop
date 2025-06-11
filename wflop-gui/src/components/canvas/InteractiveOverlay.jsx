import React from 'react';

export const InteractiveOverlay = ({ 
  dimension, 
  cellSize, 
  canvasSize, 
  currentLayout, 
  isEvaluating, 
  onCellClick,
  getCellCenter
}) => {
  const handleClick = (event) => {
    if (isEvaluating) {
      return;
    }

    const rect = event.currentTarget.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    
    const col = Math.floor(x / cellSize);
    const row = Math.floor(y / cellSize);
    
    if (col >= 0 && col < dimension && row >= 0 && row < dimension) {
      const cellIndex = row * dimension + col;
      
      if (typeof onCellClick === 'function') {
        onCellClick(cellIndex);
      }
    }
  };

  const handleMouseMove = (event) => {
    if (isEvaluating) return;

    const rect = event.currentTarget.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    
    const col = Math.floor(x / cellSize);
    const row = Math.floor(y / cellSize);

    // Update cursor based on valid cell
    if (col >= 0 && col < dimension && row >= 0 && row < dimension) {
      event.currentTarget.style.cursor = 'pointer';
    } else {
      event.currentTarget.style.cursor = 'default';
    }
  };

  return (
    <>
      {/* Background grid - behind PIXI canvas but visible */}
      <div
        style={{
          position: 'absolute',
          top: 0,
          left: 0,
          width: canvasSize,
          height: canvasSize,
          zIndex: 1,
          pointerEvents: 'none'
        }}
      >
        <svg
          width={canvasSize}
          height={canvasSize}
          style={{
            position: 'absolute',
            top: 0,
            left: 0
          }}
        >
          {/* Grid lines */}
          {Array.from({ length: dimension + 1 }, (_, i) => (
            <g key={`grid-${i}`}>
              <line
                x1={Math.floor(i * cellSize)}
                y1={0}
                x2={Math.floor(i * cellSize)}
                y2={canvasSize}
                stroke="#cccccc"
                strokeWidth="1"
                opacity="0.8"
              />
              <line
                x1={0}
                y1={Math.floor(i * cellSize)}
                x2={canvasSize}
                y2={Math.floor(i * cellSize)}
                stroke="#cccccc"
                strokeWidth="1"
                opacity="0.8"
              />
            </g>
          ))}
          
          {/* Highlight turbine cells */}
          {currentLayout?.turbineIndices?.map((cellIndex) => {
            const row = Math.floor(cellIndex / dimension);
            const col = cellIndex % dimension;
            const x = Math.floor(col * cellSize);
            const y = Math.floor(row * cellSize);
            
            return (
              <rect
                key={`highlight-${cellIndex}`}
                x={x}
                y={y}
                width={Math.floor(cellSize)}
                height={Math.floor(cellSize)}
                fill="#90EE90"
                opacity="0.25"
              />
            );
          })}
        </svg>
      </div>

      {/* Invisible click handler - on top for interactions */}
      <div
        onClick={handleClick}
        onMouseMove={handleMouseMove}
        style={{
          position: 'absolute',
          top: 0,
          left: 0,
          width: canvasSize,
          height: canvasSize,
          backgroundColor: 'transparent',
          cursor: isEvaluating ? 'wait' : 'pointer',
          zIndex: 10,
          pointerEvents: 'auto'
        }}
      />
    </>
  );
};