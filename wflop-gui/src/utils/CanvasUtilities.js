export const calculateEntrainmentConstant = (hubHeight, surfaceRoughness) => {
  return 0.5 / Math.log(hubHeight / surfaceRoughness);
};

export const calculateCanvasSize = (dimension) => {
  const maxCanvasSize = Math.min(600, window.innerWidth * 0.8, window.innerHeight * 0.6);
  const cellSize = maxCanvasSize / dimension;
  const canvasSize = dimension * cellSize;
  return { canvasSize, cellSize };
};

export const scaleCoordinates = (coordinates, scaleFactor) => {
  return coordinates.map(({ x, y }) => ({
    x: x * scaleFactor,
    y: y * scaleFactor
  }));
};