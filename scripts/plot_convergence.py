import os
import pandas as pd
import matplotlib.pyplot as plt

# 🔧 HARD-CODED PATH (CHANGE THIS)
FOLDER_PATH = "wflop-experiments/results"

def main():
    plt.figure(figsize=(10, 6))

    for filename in os.listdir(FOLDER_PATH):
        if filename.endswith(".csv"):
            path = os.path.join(FOLDER_PATH, filename)

            df = pd.read_csv(path)

            label = os.path.splitext(filename)[0]

            plt.plot(df["x"], df["best_fitness"], label=label)

    plt.xlabel("Time / Iteration")
    plt.ylabel("Best Fitness")
    plt.title("Algorithm Convergence")
    plt.legend()
    plt.grid(True)
    plt.tight_layout()

    plt.show()

if __name__ == "__main__":
    main()
