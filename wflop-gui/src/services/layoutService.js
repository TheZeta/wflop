import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/wflop';

export const evaluateLayout = async (problem, turbineIndices) => {
  // Validation
  const maxCellIndex = problem.dimension * problem.dimension - 1;
  const invalidIndices = turbineIndices.filter(idx => idx < 0 || idx > maxCellIndex);
  
  if (invalidIndices.length > 0) {
    throw new Error(`Invalid turbine indices: ${invalidIndices.join(', ')}`);
  }

  const cellCount = problem.dimension * problem.dimension;
  if (turbineIndices.length > cellCount) {
    throw new Error(`Too many turbines: ${turbineIndices.length} > ${cellCount}`);
  }

  const requestPayload = {
    problem,
    solution: {
      layout: turbineIndices,
      fitness: 0
    }
  };

  try {
    const response = await axios.post(`${API_BASE_URL}/evaluate`, requestPayload);
    return response.data;
  } catch (error) {
    if (error.response?.status === 500) {
      throw new Error('Server error - likely an issue with fitness calculation');
    }
    throw new Error('Network error - please check if the backend is running');
  }
};