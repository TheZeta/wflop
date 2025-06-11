import React from 'react';

const ExternalCompass = () => (
  <div style={{
    position: 'relative',
    width: '60px',
    height: '60px',
    border: '2px solid #333',
    borderRadius: '50%',
    backgroundColor: 'rgba(255, 255, 255, 0.9)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '12px',
    fontWeight: 'bold',
    boxShadow: '0 2px 4px rgba(0,0,0,0.2)'
  }}>
    <div style={{
      position: 'absolute',
      top: '5px',
      fontSize: '14px',
      fontWeight: 'bold',
      color: '#333'
    }}>
      N
    </div>
    <div style={{
      width: '2px',
      height: '20px',
      backgroundColor: '#333',
      position: 'absolute',
      top: '8px'
    }} />
  </div>
);

const WindDirectionArrow = ({ selectedWind }) => {
  const windFromRad = (90 - selectedWind.angle) * Math.PI / 180;
  const arrowLength = 25;
  const arrowX = 30;
  const arrowY = 30;
  
  const endX = arrowX + Math.cos(windFromRad) * arrowLength;
  const endY = arrowY + Math.sin(windFromRad) * arrowLength;
  
  const headLength = 6;
  const headAngle = Math.PI / 6;
  
  return (
    <svg 
      width="60" 
      height="60" 
      style={{ 
        position: 'absolute', 
        top: 0, 
        left: 0,
        pointerEvents: 'none'
      }}
    >
      {/* Main arrow line */}
      <line 
        x1={arrowX} 
        y1={arrowY} 
        x2={endX} 
        y2={endY} 
        stroke="#0066cc" 
        strokeWidth="3"
      />
      {/* Arrow head */}
      <line 
        x1={endX} 
        y1={endY}
        x2={endX - headLength * Math.cos(windFromRad - headAngle)}
        y2={endY - headLength * Math.sin(windFromRad - headAngle)}
        stroke="#0066cc" 
        strokeWidth="3"
      />
      <line 
        x1={endX} 
        y1={endY}
        x2={endX - headLength * Math.cos(windFromRad + headAngle)}
        y2={endY - headLength * Math.sin(windFromRad + headAngle)}
        stroke="#0066cc" 
        strokeWidth="3"
      />
    </svg>
  );
};

export const WindInfoPanel = ({ selectedWind, isEvaluating }) => (
  <div style={{ width: '100%' }}>
    {/* Wind Info Section */}
    <div style={{
      padding: '15px',
      backgroundColor: isEvaluating ? '#fff3cd' : '#f8f9fa',
      borderRadius: '8px',
      border: '1px solid #e9ecef',
      marginBottom: '15px'
    }}>
      <div style={{ fontSize: '14px', color: '#495057', marginBottom: '10px' }}>
        <strong>Wind:</strong> {selectedWind.speed} m/s from {selectedWind.angle}° 
        <div style={{ fontSize: '12px', color: '#6c757d', marginTop: '2px' }}>
          (0° = North, counterclockwise)
        </div>
        {isEvaluating && (
          <div style={{ fontSize: '12px', color: '#856404', marginTop: '5px' }}>
            ⏳ Evaluating layout...
          </div>
        )}
      </div>
      
      {/* Compass and Wind Direction */}
      <div style={{ 
        display: 'flex', 
        alignItems: 'center', 
        gap: '15px',
        justifyContent: 'center'
      }}>
        <div style={{ 
          position: 'relative',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: '5px'
        }}>
          <div style={{ position: 'relative' }}>
            <ExternalCompass />
            <WindDirectionArrow selectedWind={selectedWind} />
          </div>
          <div style={{ 
            fontSize: '11px', 
            color: '#6c757d',
            textAlign: 'center'
          }}>
            Wind Direction
          </div>
        </div>
      </div>
    </div>

    {/* Interactive Mode Info */}
    <div style={{
      padding: '12px',
      backgroundColor: '#e7f3ff',
      borderRadius: '6px',
      fontSize: '13px',
      color: '#0056b3',
      border: '1px solid #b3d7ff'
    }}>
      💡 <strong>Interactive Mode:</strong> Click any cell to add/remove turbines. Layout will be re-evaluated automatically.
    </div>
  </div>
);