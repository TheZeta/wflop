// src/utils/layoutUtils.js

export function getTurbineCoordinates(layout, dimension, gridWidth) {
  // Handle both layout formats: array directly or object with turbineIndices
  const turbineIndices = Array.isArray(layout) ? layout : layout.turbineIndices || [];
  
  return turbineIndices.map((index) => {
    const x = (index % dimension + 0.5) * gridWidth;
    const y = (Math.floor(index / dimension) + 0.5) * gridWidth;
    return { x, y };
  });
}