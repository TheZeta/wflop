import json
import itertools
import os
from collections import OrderedDict

# Define the parameter grids
population_vals = [50, 100, 200]
crossover_vals = [0.3, 0.5, 0.7]
mutation_vals = [0.05, 0.1, 0.2]
wake_analysis_vals = [0.1, 0.3, 0.5]
mutation_selection_vals = [0.2, 0.4, 0.6]
smart_mutation_vals = [0.2, 0.4, 0.8]

# Define the specific nested directory
output_dir = "configs/tuning"
os.makedirs(output_dir, exist_ok=True)

def generate_configs():
    combinations = itertools.product(
        population_vals,
        crossover_vals,
        mutation_vals,
        wake_analysis_vals,
        mutation_selection_vals,
        smart_mutation_vals
    )

    for pop_p, cross_p, mut_p, wake_p, mut_sel_p, smart_m in combinations:
        config = OrderedDict([
            ("algorithm", "WDGA"),
            ("populationSize", pop_p),
            ("crossoverRate", cross_p),
            ("mutationRate", mut_p),
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
            f"pop{pop_p}_"
            f"cr{cross_p}_"
            f"mut{mut_p}_"
            f"wa{wake_p}_"
            f"ms{mut_sel_p}_"
            f"sm{smart_m}.json"
        )
        filepath = os.path.join(output_dir, filename)

        with open(filepath, 'w') as f:
            json.dump(config, f, indent=2)

        print('{ "id": "'
            f"pop{pop_p}_"
            f"cr{cross_p}_"
            f"mut{mut_p}_"
            f"wa{wake_p}_"
            f"ms{mut_sel_p}_"
            f"sm{smart_m}"
            '", "path": "'
            f"{filepath}"
            '" },'
        )

if __name__ == "__main__":
    generate_configs()
