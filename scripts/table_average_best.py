import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

CSV_PATH = "wflop-experiments/results/average-best.csv"

# Metric configuration
METRICS = [
    {
        "key": "mean_best_fitness",
        "label": "Average Best Fitness (maximize)",
        "round": 2,
        "maximize": True,
    },
    {
        "key": "mean_best_found_at",
        "label": "Best Found At (minimize)",
        "round": 1,
        "maximize": False,
    },
    {
        "key": "conversion_efficiency",
        "label": "Conversion Efficiency (maximize)",
        "round": 3,
        "maximize": True,
    },
]


def build_table(df, metric_cfg):
    table_df = df.pivot(
        index="problem",
        columns="algorithm",
        values=metric_cfg["key"]
    ).round(metric_cfg["round"])

    return table_df


def draw_table(ax, table_df, metric_cfg):
    ax.clear()
    ax.axis("off")

    n_rows, n_cols = table_df.shape

    mpl_table = ax.table(
        cellText=table_df.values,
        rowLabels=table_df.index,
        colLabels=table_df.columns,
        loc="center"
    )

    mpl_table.auto_set_font_size(False)
    mpl_table.set_fontsize(10)
    mpl_table.scale(1, 1.5)

    # Rotate column headers
    for col_idx in range(n_cols):
        header_cell = mpl_table[0, col_idx]
        header_cell.get_text().set_rotation(90)
        header_cell.get_text().set_ha("center")
        header_cell.get_text().set_va("bottom")

    # Row-wise coloring
    for row_idx in range(n_rows):
        row = table_df.iloc[row_idx].values

        best_val = np.max(row) if metric_cfg["maximize"] else np.min(row)
        worst_val = np.min(row) if metric_cfg["maximize"] else np.max(row)

        for col_idx in range(n_cols):
            cell = mpl_table[row_idx + 1, col_idx]
            value = row[col_idx]

            if value == best_val:
                cell.set_facecolor("#c8e6c9")  # green
            elif value == worst_val:
                cell.set_facecolor("#ffcdd2")  # red
            else:
                cell.set_facecolor("#ffffff")

    ax.set_title(
        f"{metric_cfg['label']}  (← / → to switch metric)",
        pad=20
    )


def main():
    df = pd.read_csv(CSV_PATH)

    fig, ax = plt.subplots(figsize=(12, 6))
    metric_index = 0

    table_df = build_table(df, METRICS[metric_index])
    draw_table(ax, table_df, METRICS[metric_index])

    def on_key(event):
        nonlocal metric_index, table_df

        if event.key == "right":
            metric_index = (metric_index + 1) % len(METRICS)
        elif event.key == "left":
            metric_index = (metric_index - 1) % len(METRICS)
        else:
            return

        table_df = build_table(df, METRICS[metric_index])
        draw_table(ax, table_df, METRICS[metric_index])
        fig.canvas.draw_idle()

    fig.canvas.mpl_connect("key_press_event", on_key)

    plt.tight_layout()
    plt.show()


if __name__ == "__main__":
    main()
