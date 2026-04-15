import json
import itertools
import os
from collections import OrderedDict

# Define the parameter grids
wake_analysis_vals = [0.1, 0.3, 0.5]
mutation_selection_vals = [0.2, 0.4, 0.6]
smart_mutation_vals = [0.2, 0.4, 0.8]

# Define the specific nested directory
output_dir = "configs/tuning"
os.makedirs(output_dir, exist_ok=True)

def generate_configs():
    combinations = itertools.product(
        wake_analysis_vals,
        mutation_selection_vals,
        smart_mutation_vals
    )

    for wake_p, mut_sel_p, smart_m in combinations:
        config = OrderedDict([
            ("algorithm", "WDGA"),
            ("populationSize", 100),
            ("crossoverRate", 0.3),
            ("mutationRate", 0.1),
            ("smartMutationRate", smart_m),
            ("selectionStrategy", "tournament"),
            ("wakeAnalysisPercentage", wake_p),
            ("mutationSelectionPercentage", mut_sel_p),
            ("termination", {
                "type": "time",
                "durationMillis": 60000
            })
        ])

        # Filename using descriptive shorthand for identification
        filename = (
            f"wa{wake_p}_"
            f"ms{mut_sel_p}_"
            f"sm{smart_m}.json"
        )
        filepath = os.path.join(output_dir, filename)

        with open(filepath, 'w') as f:
            json.dump(config, f, indent=2)

        print('{ "id": "'
            f"wa{wake_p}_"
            f"ms{mut_sel_p}_"
            f"sm{smart_m}"
            '", "path": "'
            f"{filepath}"
            '" },'
        )

if __name__ == "__main__":
    generate_configs()
