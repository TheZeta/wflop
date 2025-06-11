import React from 'react';

export const CanvasLegend = () => (
  <div style={{
    display: 'flex',
    justifyContent: 'center',
    gap: '20px',
    marginBottom: '10px',
    fontSize: '14px',
    color: '#666'
  }}>
    <span style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
      <div style={{
        width: '12px',
        height: '12px',
        backgroundColor: '#ff6b6b',
        opacity: 0.7,
        borderRadius: '2px'
      }} />
      Wake Cones
    </span>
    <span style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
      <div style={{
        width: '20px',
        height: '3px',
        backgroundColor: '#0066cc',
        borderRadius: '1px'
      }} />
      Wind Flow
    </span>
    <span style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
      <div style={{
        width: '12px',
        height: '12px',
        backgroundColor: '#90EE90',
        opacity: 0.5,
        borderRadius: '2px'
      }} />
      Turbine Cells
    </span>
  </div>
);