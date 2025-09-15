package org.zafer.wflopmetaheuristic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ResultEntry Tests")
public class ResultEntryTest {

    @Test
    @DisplayName("Should create ResultEntry with all required fields")
    public void shouldCreateResultEntryWithAllFields() {
        // Arrange
        String algorithm = "GA";
        String configuration = "crossover=0.7,mutation=0.3";
        int runNumber = 1;
        double fitness = 123.45;
        double runtimeSeconds = 2.5;
        String solution = "[1,2,3,4,5]";

        // Act
        ResultEntry entry = new ResultEntry(algorithm, configuration, runNumber, fitness, runtimeSeconds, solution);

        // Assert
        assertEquals(algorithm, entry.algorithm());
        assertEquals(configuration, entry.configuration());
        assertEquals(runNumber, entry.runNumber());
        assertEquals(fitness, entry.fitness(), 0.0001);
        assertEquals(runtimeSeconds, entry.runtimeSeconds(), 0.0001);
        assertEquals(solution, entry.solution());
    }

    @Test
    @DisplayName("Should generate correct CSV header")
    public void shouldGenerateCorrectCsvHeader() {
        // Act
        String header = ResultEntry.csvHeader();

        // Assert
        assertEquals("algorithm,configuration,runNumber,fitness,runtimeSeconds,solution", header);
    }

    @Test
    @DisplayName("Should convert to CSV row correctly")
    public void shouldConvertToCsvRowCorrectly() {
        // Arrange
        ResultEntry entry = new ResultEntry("GA", "config1", 1, 123.456789, 2.123, "[1,2,3]");

        // Act
        String csvRow = entry.toCsvRow();

        // Assert
        assertEquals("GA,config1,1,123.456789,2.123,\"[1,2,3]\"", csvRow);
    }

    @Test
    @DisplayName("Should handle quotes in solution for CSV")
    public void shouldHandleQuotesInSolutionForCsv() {
        // Arrange
        ResultEntry entry = new ResultEntry("GA", "config1", 1, 123.45, 2.12, "solution with \"quotes\"");

        // Act
        String csvRow = entry.toCsvRow();

        // Assert
        assertTrue(csvRow.contains("\"solution with \"\"quotes\"\"\""));
    }

    @Test
    @DisplayName("Should create ResultEntry from RunResult")
    public void shouldCreateResultEntryFromRunResult() {
        // Arrange - Create a mock solution and metrics
        TestSolution solution = new TestSolution(100.0);
        RunMetrics metrics = new RunMetrics(0, 2500, 100, 100.0); // 2.5 seconds runtime
        RunResult<TestSolution> runResult = new RunResult<>(solution, metrics);

        // Act
        ResultEntry entry = ResultEntry.fromRunResult("GA", "test-config", 1, runResult);

        // Assert
        assertEquals("GA", entry.algorithm());
        assertEquals("test-config", entry.configuration());
        assertEquals(1, entry.runNumber());
        assertEquals(100.0, entry.fitness(), 0.0001);
        assertEquals(2.5, entry.runtimeSeconds(), 0.0001);
        assertEquals(solution.toString(), entry.solution());
    }

    @Test
    @DisplayName("Should create ResultEntry from RunResult with custom solution representation")
    public void shouldCreateResultEntryFromRunResultWithCustomSolution() {
        // Arrange
        TestSolution solution = new TestSolution(100.0);
        RunMetrics metrics = new RunMetrics(0, 3000, 150, 100.0); // 3.0 seconds runtime
        RunResult<TestSolution> runResult = new RunResult<>(solution, metrics);
        String customSolution = "{\"indices\":[1,2,3,4,5],\"fitness\":100.0}";

        // Act
        ResultEntry entry = ResultEntry.fromRunResult("PSO", "particles=50", 2, runResult, customSolution);

        // Assert
        assertEquals("PSO", entry.algorithm());
        assertEquals("particles=50", entry.configuration());
        assertEquals(2, entry.runNumber());
        assertEquals(100.0, entry.fitness(), 0.0001);
        assertEquals(3.0, entry.runtimeSeconds(), 0.0001);
        assertEquals(customSolution, entry.solution());
    }

    // Test helper class
    private static class TestSolution implements Solution {
        private final double fitness;

        public TestSolution(double fitness) {
            this.fitness = fitness;
        }

        @Override
        public double getFitness() {
            return fitness;
        }

        @Override
        public String toString() {
            return "TestSolution{fitness=" + fitness + "}";
        }
    }
}