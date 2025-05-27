package org.zafer.wflopconfig;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ConfigLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static <T> T loadFromResource(String resourcePath, TypeReference<T> typeRef) {
        return loadFromResource(resourcePath, typeRef, Thread.currentThread().getContextClassLoader());
    }

    public static <T> T loadFromResource(String resourcePath, TypeReference<T> typeRef, ClassLoader loader) {
        try (InputStream is = loader.getResourceAsStream(resourcePath)) {
            if (is == null) throw new FileNotFoundException(resourcePath);
            return MAPPER.readValue(is, typeRef);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load: " + resourcePath, e);
        }
    }
}
