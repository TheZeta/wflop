import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

CSV_PATH = "wflop-experiments/results/average-best.csv"

def main():
    df = pd.read_csv(CSV_PATH)

    table_df = df.pivot(
        index="problem",
        columns="algorithm",
        values="mean_best_fitness"
    ).round(2)

    n_rows, n_cols = table_df.shape

    fig, ax = plt.subplots(figsize=(12, 0.6 * n_rows + 2))
    ax.axis("off")

    mpl_table = ax.table(
        cellText=table_df.values,
        rowLabels=table_df.index,
        colLabels=table_df.columns,
        loc="center"
    )

    mpl_table.auto_set_font_size(False)
    mpl_table.set_fontsize(10)
    mpl_table.scale(1, 1.5)

    # 🎨 Row-wise coloring
    for row_idx in range(n_rows):
        row = table_df.iloc[row_idx].values
        min_val = np.max(row)
        max_val = np.min(row)

        for col_idx in range(n_cols):
            cell = mpl_table[row_idx + 1, col_idx]
            value = row[col_idx]

            if value == min_val:
                cell.set_facecolor("#c8e6c9")  # light green
            elif value == max_val:
                cell.set_facecolor("#ffcdd2")  # light red

    ax.set_title("Average Best Fitness per Problem", pad=20)

    plt.tight_layout()
    plt.show()

if __name__ == "__main__":
    main()
