package org.zafer.wflopmetaheuristic;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for exporting experiment results to various formats.
 * Supports CSV and JSON export for statistical analysis and visualization.
 */
public class ResultExporter {
    
    /**
     * Exports a list of ResultEntry objects to a CSV file.
     * 
     * @param results the results to export
     * @param filePath the path where to save the CSV file
     * @throws IOException if file writing fails
     */
    public static void exportToCsv(List<ResultEntry> results, Path filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            // Write header
            writer.write(ResultEntry.csvHeader());
            writer.write(System.lineSeparator());
            
            // Write data rows
            for (ResultEntry result : results) {
                writer.write(result.toCsvRow());
                writer.write(System.lineSeparator());
            }
        }
    }
    
    /**
     * Exports a list of ResultEntry objects to a JSON file.
     * Simple JSON array format without external dependencies.
     * 
     * @param results the results to export
     * @param filePath the path where to save the JSON file
     * @throws IOException if file writing fails
     */
    public static void exportToJson(List<ResultEntry> results, Path filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            writer.write("[\n");
            
            for (int i = 0; i < results.size(); i++) {
                ResultEntry result = results.get(i);
                writer.write("  {\n");
                writer.write(String.format("    \"algorithm\": \"%s\",\n", escapeJson(result.algorithm())));
                writer.write(String.format("    \"configuration\": \"%s\",\n", escapeJson(result.configuration())));
                writer.write(String.format("    \"runNumber\": %d,\n", result.runNumber()));
                writer.write(String.format("    \"fitness\": %.6f,\n", result.fitness()));
                writer.write(String.format("    \"runtimeSeconds\": %.3f,\n", result.runtimeSeconds()));
                writer.write(String.format("    \"solution\": \"%s\"\n", escapeJson(result.solution())));
                writer.write("  }");
                
                if (i < results.size() - 1) {
                    writer.write(",");
                }
                writer.write("\n");
            }
            
            writer.write("]\n");
        }
    }
    
    /**
     * Groups results by algorithm and configuration for analysis.
     * Returns a summary string with statistics for each group.
     */
    public static String generateSummaryReport(List<ResultEntry> results) {
        StringBuilder report = new StringBuilder();
        report.append("=== EXPERIMENT SUMMARY REPORT ===\n\n");
        
        // Group by algorithm and configuration
        Map<String, List<ResultEntry>> groupedResults = results.stream()
                .collect(Collectors.groupingBy(
                        r -> r.algorithm() + " - " + r.configuration()
                ));
        
        for (Map.Entry<String, List<ResultEntry>> entry : groupedResults.entrySet()) {
            String configName = entry.getKey();
            List<ResultEntry> configResults = entry.getValue();
            
            double avgFitness = configResults.stream()
                    .mapToDouble(ResultEntry::fitness)
                    .average()
                    .orElse(0.0);
            
            double maxFitness = configResults.stream()
                    .mapToDouble(ResultEntry::fitness)
                    .max()
                    .orElse(0.0);
            
            double minFitness = configResults.stream()
                    .mapToDouble(ResultEntry::fitness)
                    .min()
                    .orElse(0.0);
            
            double avgRuntime = configResults.stream()
                    .mapToDouble(ResultEntry::runtimeSeconds)
                    .average()
                    .orElse(0.0);
            
            report.append(String.format("%s:\n", configName));
            report.append(String.format("  Runs: %d\n", configResults.size()));
            report.append(String.format("  Average Fitness: %.2f MW\n", avgFitness));
            report.append(String.format("  Best Fitness: %.2f MW\n", maxFitness));
            report.append(String.format("  Worst Fitness: %.2f MW\n", minFitness));
            report.append(String.format("  Average Runtime: %.2f seconds\n", avgRuntime));
            report.append("\n");
        }
        
        return report.toString();
    }
    
    /**
     * Escapes special characters for JSON output.
     */
    private static String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}