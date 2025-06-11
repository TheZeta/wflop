import React, { useState } from 'react';
import axios from 'axios';

const defaultProblem = {
  rotorRadius: 40.0,
  hubHeight: 100.0,
  rotorEfficiency: 0.9,
  thrustCoefficient: 0.8,
  airDensity: 1.225,
  surfaceRoughness: 0.1,
  gridWidth: 200.0,
  dimension: 10,
  numberOfTurbines: 20,
  windProfiles: [
    { speed: 5.2, angle: 45 },
    { speed: 6.7, angle: 90 },
    { speed: 4.9, angle: 135 },
    { speed: 7.3, angle: 180 },
    { speed: 5.8, angle: 225 },
  ]
};

const LayoutForm = ({ onResult }) => {
  const [problem, setProblem] = useState(defaultProblem);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    try {
      setLoading(true);
      const res = await axios.post('http://localhost:8080/api/wflop/solve', problem);
      onResult(JSON.parse(JSON.stringify({ solution: res.data, problem })));
    } catch (e) {
      alert('API request failed.');
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <button onClick={handleSubmit} disabled={loading}>
        {loading ? 'Solving...' : 'Run GA & Visualize'}
      </button>
    </div>
  );
};

export default LayoutForm;