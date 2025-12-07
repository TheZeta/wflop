package org.zafer.wflopmetaheuristic.termination;

public class TerminationConditionFactory {

    public static TerminationCondition fromConfig(TerminationConditionConfig config) {
        TerminationCondition terminationCondition = null;

        switch (config.getType()) {
            case "generation":
                terminationCondition = new GenerationBasedTermination(config.getMaxGenerations());
                break;
            case "time":
                terminationCondition = new TimeBasedTermination(config.getDurationMillis());
                break;
            default:
                throw new IllegalArgumentException("Unknown termination type: " + config.getType());
        }

        return terminationCondition;
    }
}
