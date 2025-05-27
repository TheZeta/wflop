import json
import os
import matplotlib.pyplot as plt

RESULTS_FILE = os.path.join("../benchmark-results", "benchmark_result.json")
OUTPUT_IMG = os.path.join("../benchmark-results", "benchmark_chart.png")

def load_results(filepath):
    with open(filepath, "r") as f:
        return json.load(f)

def format_label(params):
    # D = useDistanceMatrix, A = useIntersectedAreaMatrix
    d = params.get("useDistanceMatrix", "false")
    a = params.get("useIntersectedAreaMatrix", "false")
    return f"D={d}, A={a}"

def plot_results(results):
    labels = []
    scores = []
    errors = []

    for entry in results:
        label = format_label(entry["params"])
        labels.append(label)
        scores.append(entry["primaryMetric"]["score"])
        errors.append(entry["primaryMetric"]["scoreError"])

    plt.figure(figsize=(10, 6))
    bars = plt.bar(labels, scores, yerr=errors, capsize=5, color='lightseagreen', edgecolor='black')

    plt.xlabel("Benchmark Configuration")
    plt.ylabel("Average Time (ms/op)")
    plt.title("WFLOP Benchmark Comparison")
    plt.grid(axis='y', linestyle='--', alpha=0.7)
    plt.tight_layout()

    for bar, score in zip(bars, scores):
        yval = bar.get_height()
        plt.text(bar.get_x() + bar.get_width() / 2, yval + 0.05, f"{score:.2f}", ha='center', va='bottom')

    plt.savefig(OUTPUT_IMG)
    print(f"Chart saved to {OUTPUT_IMG}")

if __name__ == "__main__":
    if not os.path.exists(RESULTS_FILE):
        print(f"Benchmark results not found at {RESULTS_FILE}")
    else:
        benchmark_results = load_results(RESULTS_FILE)
        plot_results(benchmark_results)
