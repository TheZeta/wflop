package org.zafer.wflopconfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static <T> T load(String path, TypeReference<T> typeRef) {
        return load(path, typeRef, Thread.currentThread().getContextClassLoader());
    }

    public static <T> T load(String path, TypeReference<T> typeRef, ClassLoader loader) {
        // 1. Try filesystem path first
        File file = new File(path);
        if (file.isFile()) {
            try (InputStream is = new FileInputStream(file)) {
                return MAPPER.readValue(is, typeRef);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load file: " + path, e);
            }
        }

        // 2. Fall back to classpath resource
        try (InputStream is = loader.getResourceAsStream(path)) {
            if (is == null) {
                throw new FileNotFoundException("Not found as file or resource: " + path);
            }
            return MAPPER.readValue(is, typeRef);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource: " + path, e);
        }
    }
}
