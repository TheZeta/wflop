package org.zafer.wflopmetaheuristic;

import java.util.Objects;

/**
 * Generic result entry for metaheuristic algorithm runs.
 * This class captures essential information about an algorithm run in a standardized format
 * that can be used for analysis, comparison, and export across different metaheuristic algorithms.
 */
public class ResultEntry {
    private final String algorithm;
    private final String configuration;
    private final int runNumber;
    private final double fitness;
    private final double runtimeSeconds;
    private final String solution; // JSON representation or string-based solution encoding

    public ResultEntry(String algorithm, String configuration, int runNumber, double fitness, double runtimeSeconds, String solution) {
        this.algorithm = algorithm;
        this.configuration = configuration;
        this.runNumber = runNumber;
        this.fitness = fitness;
        this.runtimeSeconds = runtimeSeconds;
        this.solution = solution;
    }

    public String algorithm() {
        return algorithm;
    }

    public String configuration() {
        return configuration;
    }

    public int runNumber() {
        return runNumber;
    }

    public double fitness() {
        return fitness;
    }

    public double runtimeSeconds() {
        return runtimeSeconds;
    }

    public String solution() {
        return solution;
    }
    
    /**
     * Creates a ResultEntry from RunResult and additional metadata.
     * 
     * @param algorithm the name of the algorithm (e.g., "GA", "PSO", "SA")
     * @param configuration string description of algorithm configuration
     * @param runNumber the run number (for multiple runs of same configuration)
     * @param runResult the RunResult containing metrics and solution
     * @return a new ResultEntry
     */
    public static <S extends Solution> ResultEntry fromRunResult(
            String algorithm,
            String configuration, 
            int runNumber,
            RunResult<S> runResult) {
        
        double runtimeSeconds = runResult.getMetrics().getDurationMs() / 1000.0;
        double fitness = runResult.getMetrics().getBestFitness();
        String solutionStr = runResult.getBestSolution().toString();
        
        return new ResultEntry(algorithm, configuration, runNumber, fitness, runtimeSeconds, solutionStr);
    }
    
    /**
     * Creates a ResultEntry with custom solution representation.
     * 
     * @param algorithm the name of the algorithm
     * @param configuration string description of algorithm configuration
     * @param runNumber the run number
     * @param runResult the RunResult containing metrics
     * @param solutionRepresentation custom string representation of the solution
     * @return a new ResultEntry
     */
    public static <S extends Solution> ResultEntry fromRunResult(
            String algorithm,
            String configuration, 
            int runNumber,
            RunResult<S> runResult,
            String solutionRepresentation) {
        
        double runtimeSeconds = runResult.getMetrics().getDurationMs() / 1000.0;
        double fitness = runResult.getMetrics().getBestFitness();
        
        return new ResultEntry(algorithm, configuration, runNumber, fitness, runtimeSeconds, solutionRepresentation);
    }
    
    /**
     * Returns a CSV header for ResultEntry.
     */
    public static String csvHeader() {
        return "algorithm,configuration,runNumber,fitness,runtimeSeconds,solution";
    }
    
    /**
     * Returns this entry as a CSV row.
     */
    public String toCsvRow() {
        return String.format("%s,%s,%d,%.6f,%.3f,\"%s\"", 
                algorithm, configuration, runNumber, fitness, runtimeSeconds, 
                solution.replace("\"", "\"\"")); // Escape quotes in solution
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResultEntry that = (ResultEntry) o;
        return runNumber == that.runNumber &&
                Double.compare(that.fitness, fitness) == 0 &&
                Double.compare(that.runtimeSeconds, runtimeSeconds) == 0 &&
                Objects.equals(algorithm, that.algorithm) &&
                Objects.equals(configuration, that.configuration) &&
                Objects.equals(solution, that.solution);
    }

    @Override
    public int hashCode() {
        return Objects.hash(algorithm, configuration, runNumber, fitness, runtimeSeconds, solution);
    }

    @Override
    public String toString() {
        return "ResultEntry{" +
                "algorithm='" + algorithm + '\'' +
                ", configuration='" + configuration + '\'' +
                ", runNumber=" + runNumber +
                ", fitness=" + fitness +
                ", runtimeSeconds=" + runtimeSeconds +
                ", solution='" + solution + '\'' +
                '}';
    }
}