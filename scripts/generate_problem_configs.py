import json
import os
from copy import deepcopy

# Output directory
OUTPUT_DIR = "problem_instances"
os.makedirs(OUTPUT_DIR, exist_ok=True)

# Base (static) parameters
BASE_INSTANCE = {
    "rotorRadius": 40.0,
    "hubHeight": 100.0,
    "rotorEfficiency": 0.9,
    "thrustCoefficient": 0.8,
    "airDensity": 1.225,
    "surfaceRoughness": 0.1,
    "gridWidth": 200.0,
}

# Dimensions to test
DIMENSIONS = [10, 20, 30, 40, 50]

# Turbine density percentages
TURBINE_PERCENTAGES = [0.2, 0.4, 0.6, 0.8]

# Wind scenarios
WIND_SCENARIOS = {
    "single_dir": [
        {
            "speed": 12.0,
            "angle": 270,
            "probability": 1.0
        }
    ],
    "uniform_dirs": [
        { "speed": 12.0, "angle":   0, "probability": 0.125 },
        { "speed": 12.0, "angle":  45, "probability": 0.125 },
        { "speed": 12.0, "angle":  90, "probability": 0.125 },
        { "speed": 12.0, "angle": 135, "probability": 0.125 },
        { "speed": 12.0, "angle": 180, "probability": 0.125 },
        { "speed": 12.0, "angle": 225, "probability": 0.125 },
        { "speed": 12.0, "angle": 270, "probability": 0.125 },
        { "speed": 12.0, "angle": 315, "probability": 0.125 }
    ],
    "varying_nonuniform": [
        { "speed": 8.0,  "angle": 270, "probability": 0.20 },
        { "speed": 10.0, "angle": 270, "probability": 0.25 },
        { "speed": 12.0, "angle": 270, "probability": 0.20 },

        { "speed": 9.0,  "angle": 240, "probability": 0.10 },
        { "speed": 11.0, "angle": 240, "probability": 0.10 },

        { "speed": 7.0,  "angle": 300, "probability": 0.05 },
        { "speed": 13.0, "angle": 300, "probability": 0.10 }
    ]
}

def generate_instances():
    for dimension in DIMENSIONS:
        cell_count = dimension * dimension

        for pct in TURBINE_PERCENTAGES:
            number_of_turbines = int(cell_count * pct)

            for scenario_name, wind_profiles in WIND_SCENARIOS.items():
                instance = deepcopy(BASE_INSTANCE)

                instance.update({
                    "dimension": dimension,
                    "numberOfTurbines": number_of_turbines,
                    "windProfiles": wind_profiles
                })

                filename = (
                    f"wf_dim{dimension}_"
                    f"turb{number_of_turbines}_"
                    f"{scenario_name}.json"
                )

                filepath = os.path.join(OUTPUT_DIR, filename)

                with open(filepath, "w") as f:
                    json.dump(instance, f, indent=2)

                print('{ "id": "'
                    f"wf_dim{dimension}_"
                    f"turb{number_of_turbines}_"
                    f"{scenario_name}"
                    '", "path": "'
                    f"{filepath}"
                    '" },'
                )

if __name__ == "__main__":
    generate_instances()
