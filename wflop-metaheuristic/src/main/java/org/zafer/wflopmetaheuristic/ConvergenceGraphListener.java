package org.zafer.wflopmetaheuristic;

import org.zafer.wflopmetaheuristic.listener.ProgressListener;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * ProgressListener that collects convergence data and exports it to files
 * for generating convergence graphs.
 * 
 * The listener collects iteration, elapsed time, best fitness, and average fitness data
 * and can export to CSV, JSON, or HTML formats. Supports plotting by iteration or by time.
 */
public class ConvergenceGraphListener implements ProgressListener {

    private final List<ConvergenceDataPoint> dataPoints = new ArrayList<>();
    private final String outputPath;
    private final ExportFormat format;
    private final String algorithmName;
    private final XAxisType xAxisType;
    private long startTimeMs = -1; // -1 indicates not started yet
    private int bestFitnessIteration = -1; // Iteration when best fitness was achieved
    private double bestFitnessTimeSeconds = -1; // Time when best fitness was achieved
    private double bestFitnessValue = Double.NEGATIVE_INFINITY; // Track the best fitness value

    /**
     * Export formats supported by the listener.
     */
    public enum ExportFormat {
        CSV,    // CSV file for external plotting tools
        JSON,   // JSON file for programmatic access
        HTML,   // HTML file with embedded interactive chart
        ALL     // Export to all formats
    }

    /**
     * X-axis type for convergence graphs.
     */
    public enum XAxisType {
        ITERATION,  // Plot by iteration number
        TIME,       // Plot by elapsed time (seconds)
        BOTH        // HTML only: allow switching between iteration and time
    }

    /**
     * Data point representing convergence at a single iteration.
     */
    public static class ConvergenceDataPoint {
        private final int iteration;
        private final double elapsedTimeSeconds;
        private final double bestFitness;
        private final double averageFitness;

        public ConvergenceDataPoint(int iteration, double elapsedTimeSeconds, double bestFitness, double averageFitness) {
            this.iteration = iteration;
            this.elapsedTimeSeconds = elapsedTimeSeconds;
            this.bestFitness = bestFitness;
            this.averageFitness = averageFitness;
        }

        public int getIteration() {
            return iteration;
        }

        public double getElapsedTimeSeconds() {
            return elapsedTimeSeconds;
        }

        public double getBestFitness() {
            return bestFitness;
        }

        public double getAverageFitness() {
            return averageFitness;
        }
    }

    /**
     * Creates a ConvergenceGraphListener that exports to CSV format, plotting by iteration.
     * 
     * @param outputPath the base path for output files (without extension)
     * @param algorithmName optional algorithm name for file naming and HTML title
     */
    public ConvergenceGraphListener(String outputPath, String algorithmName) {
        this(outputPath, algorithmName, ExportFormat.CSV, XAxisType.ITERATION);
    }

    /**
     * Creates a ConvergenceGraphListener with specified export format, plotting by iteration.
     * 
     * @param outputPath the base path for output files (without extension)
     * @param algorithmName optional algorithm name for file naming and HTML title
     * @param format the export format (CSV, JSON, HTML, or ALL)
     */
    public ConvergenceGraphListener(String outputPath, String algorithmName, ExportFormat format) {
        this(outputPath, algorithmName, format, XAxisType.ITERATION);
    }

    /**
     * Creates a ConvergenceGraphListener with specified export format and x-axis type.
     * 
     * @param outputPath the base path for output files (without extension)
     * @param algorithmName optional algorithm name for file naming and HTML title
     * @param format the export format (CSV, JSON, HTML, or ALL)
     * @param xAxisType the x-axis type (ITERATION, TIME, or BOTH for HTML)
     */
    public ConvergenceGraphListener(String outputPath, String algorithmName, ExportFormat format, XAxisType xAxisType) {
        this.outputPath = outputPath;
        this.algorithmName = algorithmName != null ? algorithmName : "Algorithm";
        this.format = format;
        this.xAxisType = xAxisType;
    }

    /**
     * Creates a ConvergenceGraphListener with default naming, plotting by iteration.
     * Output files will be named "convergence_graph" with appropriate extensions.
     */
    public ConvergenceGraphListener() {
        this("convergence_graph", "Algorithm", ExportFormat.CSV, XAxisType.ITERATION);
    }

    @Override
    public void onIteration(ProgressEvent event) {
        // Record start time on first iteration
        if (startTimeMs == -1) {
            startTimeMs = System.currentTimeMillis();
        }
        
        // Calculate elapsed time in seconds
        long currentTimeMs = System.currentTimeMillis();
        double elapsedTimeSeconds = (currentTimeMs - startTimeMs) / 1000.0;
        
        // Track when best fitness was achieved
        if (event.getBestFitness() > bestFitnessValue) {
            bestFitnessValue = event.getBestFitness();
            bestFitnessIteration = event.getIteration();
            bestFitnessTimeSeconds = elapsedTimeSeconds;
        }
        
        dataPoints.add(new ConvergenceDataPoint(
            event.getIteration(),
            elapsedTimeSeconds,
            event.getBestFitness(),
            event.getAverageFitness()
        ));
    }

    /**
     * Exports the collected convergence data to files.
     * Should be called after the algorithm has finished running.
     * 
     * @throws IOException if file writing fails
     */
    public void export() throws IOException {
        if (dataPoints.isEmpty()) {
            throw new IllegalStateException("No convergence data collected. " +
                "Make sure the algorithm has been run with this listener.");
        }

        switch (format) {
            case CSV:
                exportToCsv();
                break;
            case JSON:
                exportToJson();
                break;
            case HTML:
                exportToHtml();
                break;
            case ALL:
                exportToCsv();
                exportToJson();
                exportToHtml();
                break;
        }
    }

    /**
     * Exports convergence data to CSV format.
     */
    private void exportToCsv() throws IOException {
        Path csvPath = Paths.get(outputPath + ".csv");
        try (FileWriter writer = new FileWriter(csvPath.toFile())) {
            // Write header - always include both iteration and time for flexibility
            writer.write("iteration,elapsed_time_seconds,best_fitness,average_fitness\n");
            
            // Write data rows
            for (ConvergenceDataPoint point : dataPoints) {
                writer.write(String.format("%d,%.6f,%.6f,%.6f\n",
                    point.getIteration(),
                    point.getElapsedTimeSeconds(),
                    point.getBestFitness(),
                    point.getAverageFitness()));
            }
        }
        System.out.println("Convergence graph exported to: " + csvPath.toAbsolutePath());
    }

    /**
     * Exports convergence data to JSON format.
     */
    private void exportToJson() throws IOException {
        Path jsonPath = Paths.get(outputPath + ".json");
        try (FileWriter writer = new FileWriter(jsonPath.toFile())) {
            writer.write("{\n");
            writer.write(String.format("  \"algorithm\": \"%s\",\n", escapeJson(algorithmName)));
            
            // Add best fitness achievement information
            if (bestFitnessIteration != -1) {
                writer.write("  \"bestFitnessAchieved\": {\n");
                writer.write(String.format("    \"iteration\": %d,\n", bestFitnessIteration));
                writer.write(String.format("    \"elapsedTimeSeconds\": %.6f,\n", bestFitnessTimeSeconds));
                writer.write(String.format("    \"fitnessValue\": %.6f\n", bestFitnessValue));
                writer.write("  },\n");
            }
            
            writer.write("  \"convergence_data\": [\n");
            
            for (int i = 0; i < dataPoints.size(); i++) {
                ConvergenceDataPoint point = dataPoints.get(i);
                writer.write("    {\n");
                writer.write(String.format("      \"iteration\": %d,\n", point.getIteration()));
                writer.write(String.format("      \"elapsedTimeSeconds\": %.6f,\n", point.getElapsedTimeSeconds()));
                writer.write(String.format("      \"bestFitness\": %.6f,\n", point.getBestFitness()));
                writer.write(String.format("      \"averageFitness\": %.6f\n", point.getAverageFitness()));
                writer.write("    }");
                
                if (i < dataPoints.size() - 1) {
                    writer.write(",");
                }
                writer.write("\n");
            }
            
            writer.write("  ]\n");
            writer.write("}\n");
        }
        System.out.println("Convergence graph exported to: " + jsonPath.toAbsolutePath());
    }

    /**
     * Exports convergence data to HTML format with embedded interactive chart.
     * Uses Chart.js via CDN, so no external dependencies are required.
     * Supports switching between iteration and time-based x-axis.
     */
    private void exportToHtml() throws IOException {
        Path htmlPath = Paths.get(outputPath + ".html");
        try (FileWriter writer = new FileWriter(htmlPath.toFile())) {
            // Determine initial x-axis type and whether to show toggle
            boolean showToggle = (xAxisType == XAxisType.BOTH);
            XAxisType initialAxis = (xAxisType == XAxisType.BOTH) ? XAxisType.ITERATION : xAxisType;
            
            writer.write("<!DOCTYPE html>\n");
            writer.write("<html>\n");
            writer.write("<head>\n");
            writer.write("  <title>Convergence Graph - " + escapeHtml(algorithmName) + "</title>\n");
            writer.write("  <script src=\"https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js\"></script>\n");
            writer.write("  <style>\n");
            writer.write("    body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }\n");
            writer.write("    .container { max-width: 1200px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
            writer.write("    h1 { color: #333; margin-bottom: 10px; }\n");
            writer.write("    .info { color: #666; margin-bottom: 20px; }\n");
            writer.write("    .controls { margin-bottom: 20px; }\n");
            writer.write("    .controls button { margin-right: 10px; padding: 8px 16px; font-size: 14px; cursor: pointer; border: 1px solid #ccc; border-radius: 4px; background-color: #f8f8f8; }\n");
            writer.write("    .controls button.active { background-color: #4CAF50; color: white; border-color: #4CAF50; }\n");
            writer.write("    canvas { max-height: 600px; }\n");
            writer.write("  </style>\n");
            writer.write("</head>\n");
            writer.write("<body>\n");
            writer.write("  <div class=\"container\">\n");
            writer.write("    <h1>Convergence Graph: " + escapeHtml(algorithmName) + "</h1>\n");
            writer.write("    <div class=\"info\">\n");
            writer.write("      <p>Total iterations: " + dataPoints.size() + "</p>\n");
            if (!dataPoints.isEmpty()) {
                double totalTime = dataPoints.get(dataPoints.size() - 1).getElapsedTimeSeconds();
                writer.write("      <p>Total time: " + String.format("%.2f", totalTime) + " seconds</p>\n");
                writer.write("      <p>Final best fitness: " + String.format("%.6f", dataPoints.get(dataPoints.size() - 1).getBestFitness()) + "</p>\n");
                
                // Display when best fitness was achieved
                if (bestFitnessIteration != -1) {
                    writer.write("      <p><strong>Best fitness achieved:</strong></p>\n");
                    writer.write("      <ul style=\"margin-top: 5px; margin-bottom: 5px;\">\n");
                    writer.write("        <li>Iteration: " + bestFitnessIteration + "</li>\n");
                    writer.write("        <li>Time: " + String.format("%.2f", bestFitnessTimeSeconds) + " seconds</li>\n");
                    writer.write("        <li>Fitness value: " + String.format("%.6f", bestFitnessValue) + "</li>\n");
                    writer.write("      </ul>\n");
                }
            }
            writer.write("    </div>\n");
            
            if (showToggle) {
                writer.write("    <div class=\"controls\">\n");
                writer.write("      <button id=\"btnIteration\" class=\"active\" onclick=\"switchToIteration()\">By Iteration</button>\n");
                writer.write("      <button id=\"btnTime\" onclick=\"switchToTime()\">By Time</button>\n");
                writer.write("    </div>\n");
            }
            
            writer.write("    <canvas id=\"convergenceChart\"></canvas>\n");
            writer.write("  </div>\n");
            writer.write("  <script>\n");
            
            // Write iteration labels
            writer.write("    const iterationLabels = [");
            for (int i = 0; i < dataPoints.size(); i++) {
                writer.write(String.valueOf(dataPoints.get(i).getIteration()));
                if (i < dataPoints.size() - 1) {
                    writer.write(", ");
                }
            }
            writer.write("];\n");
            
            // Write time labels
            writer.write("    const timeLabels = [");
            for (int i = 0; i < dataPoints.size(); i++) {
                writer.write(String.format("%.2f", dataPoints.get(i).getElapsedTimeSeconds()));
                if (i < dataPoints.size() - 1) {
                    writer.write(", ");
                }
            }
            writer.write("];\n");
            
            // Write fitness data
            writer.write("    const bestFitnessData = [");
            for (int i = 0; i < dataPoints.size(); i++) {
                writer.write(String.format("%.6f", dataPoints.get(i).getBestFitness()));
                if (i < dataPoints.size() - 1) {
                    writer.write(", ");
                }
            }
            writer.write("];\n");
            
            writer.write("    const averageFitnessData = [");
            for (int i = 0; i < dataPoints.size(); i++) {
                writer.write(String.format("%.6f", dataPoints.get(i).getAverageFitness()));
                if (i < dataPoints.size() - 1) {
                    writer.write(", ");
                }
            }
            writer.write("];\n");
            
            // Determine initial labels and x-axis title
            String initialLabels = (initialAxis == XAxisType.TIME) ? "timeLabels" : "iterationLabels";
            String initialXTitle = (initialAxis == XAxisType.TIME) ? "'Time (seconds)'" : "'Iteration'";
            
            writer.write("    const ctx = document.getElementById('convergenceChart').getContext('2d');\n");
            writer.write("    let currentXAxis = '" + initialAxis.name().toLowerCase() + "';\n");
            writer.write("    \n");
            writer.write("    const chartData = {\n");
            writer.write("      labels: " + initialLabels + ",\n");
            writer.write("      datasets: [\n");
            writer.write("        {\n");
            writer.write("          label: 'Best Fitness',\n");
            writer.write("          data: bestFitnessData,\n");
            writer.write("          borderColor: 'rgb(75, 192, 192)',\n");
            writer.write("          backgroundColor: 'rgba(75, 192, 192, 0.2)',\n");
            writer.write("          tension: 0.1\n");
            writer.write("        },\n");
            writer.write("        {\n");
            writer.write("          label: 'Average Fitness',\n");
            writer.write("          data: averageFitnessData,\n");
            writer.write("          borderColor: 'rgb(255, 99, 132)',\n");
            writer.write("          backgroundColor: 'rgba(255, 99, 132, 0.2)',\n");
            writer.write("          tension: 0.1\n");
            writer.write("        }\n");
            writer.write("      ]\n");
            writer.write("    };\n");
            writer.write("    \n");
            writer.write("    const config = {\n");
            writer.write("      type: 'line',\n");
            writer.write("      data: chartData,\n");
            writer.write("      options: {\n");
            writer.write("        responsive: true,\n");
            writer.write("        maintainAspectRatio: true,\n");
            writer.write("        scales: {\n");
            writer.write("          y: {\n");
            writer.write("            beginAtZero: false,\n");
            writer.write("            title: {\n");
            writer.write("              display: true,\n");
            writer.write("              text: 'Fitness'\n");
            writer.write("            }\n");
            writer.write("          },\n");
            writer.write("          x: {\n");
            writer.write("            title: {\n");
            writer.write("              display: true,\n");
            writer.write("              text: " + initialXTitle + "\n");
            writer.write("            }\n");
            writer.write("          }\n");
            writer.write("        },\n");
            writer.write("        plugins: {\n");
            writer.write("          legend: {\n");
            writer.write("            display: true,\n");
            writer.write("            position: 'top'\n");
            writer.write("          },\n");
            writer.write("          title: {\n");
            writer.write("            display: true,\n");
            writer.write("            text: 'Algorithm Convergence'\n");
            writer.write("          }\n");
            writer.write("        }\n");
            writer.write("      }\n");
            writer.write("    };\n");
            writer.write("    \n");
            writer.write("    const chart = new Chart(ctx, config);\n");
            
            if (showToggle) {
                writer.write("    \n");
                writer.write("    function switchToIteration() {\n");
                writer.write("      currentXAxis = 'iteration';\n");
                writer.write("      chart.data.labels = iterationLabels;\n");
                writer.write("      chart.options.scales.x.title.text = 'Iteration';\n");
                writer.write("      chart.update();\n");
                writer.write("      document.getElementById('btnIteration').classList.add('active');\n");
                writer.write("      document.getElementById('btnTime').classList.remove('active');\n");
                writer.write("    }\n");
                writer.write("    \n");
                writer.write("    function switchToTime() {\n");
                writer.write("      currentXAxis = 'time';\n");
                writer.write("      chart.data.labels = timeLabels;\n");
                writer.write("      chart.options.scales.x.title.text = 'Time (seconds)';\n");
                writer.write("      chart.update();\n");
                writer.write("      document.getElementById('btnTime').classList.add('active');\n");
                writer.write("      document.getElementById('btnIteration').classList.remove('active');\n");
                writer.write("    }\n");
            }
            
            writer.write("  </script>\n");
            writer.write("</body>\n");
            writer.write("</html>\n");
        }
        System.out.println("Convergence graph exported to: " + htmlPath.toAbsolutePath());
    }

    /**
     * Gets the collected convergence data points.
     * 
     * @return a copy of the data points list
     */
    public List<ConvergenceDataPoint> getDataPoints() {
        return new ArrayList<>(dataPoints);
    }

    /**
     * Gets the number of data points collected.
     */
    public int getDataPointCount() {
        return dataPoints.size();
    }

    /**
     * Gets the iteration when the best fitness was achieved.
     * 
     * @return the iteration number, or -1 if no data has been collected
     */
    public int getBestFitnessIteration() {
        return bestFitnessIteration;
    }

    /**
     * Gets the elapsed time (in seconds) when the best fitness was achieved.
     * 
     * @return the elapsed time in seconds, or -1 if no data has been collected
     */
    public double getBestFitnessTimeSeconds() {
        return bestFitnessTimeSeconds;
    }

    /**
     * Gets the best fitness value achieved.
     * 
     * @return the best fitness value, or Double.NEGATIVE_INFINITY if no data has been collected
     */
    public double getBestFitnessValue() {
        return bestFitnessValue;
    }

    /**
     * Escapes special characters for JSON output.
     */
    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    /**
     * Escapes special characters for HTML output.
     */
    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}

