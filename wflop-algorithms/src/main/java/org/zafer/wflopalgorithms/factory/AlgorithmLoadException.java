package org.zafer.wflopalgorithms.factory;

/**
 * Exception thrown when an algorithm cannot be loaded from JSON.
 */
public class AlgorithmLoadException extends Exception {

    public AlgorithmLoadException(String message) {
        super(message);
    }

    public AlgorithmLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}


