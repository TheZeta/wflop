import os
import pandas as pd
import matplotlib.pyplot as plt
from collections import defaultdict

FOLDER_PATH = "wflop-experiments/results"

def load_data():
    grouped = defaultdict(list)

    for filename in os.listdir(FOLDER_PATH):
        if not (filename.startswith("convergence_") and filename.endswith(".csv")):
            continue

        name = os.path.splitext(filename)[0]
        parts = name.split("_")

        if len(parts) < 3:
            continue

        problem_id = parts[-2]
        algorithm_id = parts[-1]

        df = pd.read_csv(os.path.join(FOLDER_PATH, filename))
        grouped[problem_id].append((algorithm_id, df))

    return list(grouped.items())


def main():
    data = load_data()
    index = 0

    fig, ax = plt.subplots(figsize=(10, 6))

    def draw():
        ax.clear()
        problem_id, entries = data[index]

        for algorithm_id, df in entries:
            ax.plot(df["x"], df["best_fitness"], label=algorithm_id)

        ax.set_title(f"Convergence — {problem_id}")
        ax.set_xlabel("Time / Iteration")
        ax.set_ylabel("Best Fitness")
        ax.legend()
        ax.grid(True)
        fig.canvas.draw_idle()

    def on_key(event):
        nonlocal index
        if event.key == "right":
            index = (index + 1) % len(data)
            draw()
        elif event.key == "left":
            index = (index - 1) % len(data)
            draw()

    fig.canvas.mpl_connect("key_press_event", on_key)

    draw()
    plt.show()


if __name__ == "__main__":
    main()
