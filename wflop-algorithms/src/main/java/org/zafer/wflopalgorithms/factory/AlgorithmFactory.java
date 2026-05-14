package org.zafer.wflopalgorithms.factory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.zafer.wflopmetaheuristic.Metaheuristic;

public class AlgorithmFactory {

    private static final String ALGORITHM_TYPE_KEY = "algorithm";

    private final ObjectMapper mapper = new ObjectMapper();
    private final AlgorithmRegistry registry;

    public AlgorithmFactory(AlgorithmRegistry registry) {
        this.registry = registry;
    }

    public Metaheuristic load(String path) throws AlgorithmLoadException {
        return load(Path.of(path));
    }

    public Metaheuristic load(Path path) throws AlgorithmLoadException {
        try (InputStream is = Files.newInputStream(path)) {
            return load(is);
        } catch (IOException e) {
            throw new AlgorithmLoadException("Failed to load JSON from file: " + path, e);
        }
    }

    public Metaheuristic load(InputStream inputStream) throws AlgorithmLoadException {
        try {
            JsonNode jsonNode = mapper.readTree(inputStream);
            return load(jsonNode);
        } catch (IOException e) {
            throw new AlgorithmLoadException("Failed to deserialize algorithm JSON", e);
        }
    }

    public Metaheuristic load(JsonNode node) throws AlgorithmLoadException, JsonProcessingException {
        JsonNode algorithmNode = node.get(ALGORITHM_TYPE_KEY);

        if (algorithmNode == null || algorithmNode.isNull()) {
            throw new AlgorithmLoadException(
                "Missing required field: '" + ALGORITHM_TYPE_KEY + "'"
            );
        }

        String algorithm = algorithmNode.asText();
        Class<? extends Metaheuristic> algorithmClass = registry.getAlgorithmClass(algorithm);
        if (algorithmClass == null) {
            throw new AlgorithmLoadException(
                "Unknown algorithm type: " + algorithm
            );
        }

        return mapper.treeToValue(node, algorithmClass);
    }
}
