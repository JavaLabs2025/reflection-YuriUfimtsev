package org.example.generator;

import org.example.classes.CyclicClassA;
import org.example.classes.DeepClass;
import org.example.classes.TestClass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

class GeneratorTests {

    @Test
    void shouldGeneratePrimitiveTypes() {
        var generator = new Generator();

        assertNotNull(generator.generateValueOfType(Integer.class));
        assertNotNull(generator.generateValueOfType(String.class));
        assertNotNull(generator.generateValueOfType(Boolean.class));
    }

    @Test
    void shouldGenerateArray() {
        var generator = new Generator();

        int[] intArray = (int[]) generator.generateValueOfType(int[].class);
        assertNotNull(intArray);
        assertTrue(intArray.length > 0);

        String[] stringArray = (String[]) generator.generateValueOfType(String[].class);
        assertNotNull(stringArray);
        assertTrue(stringArray.length > 0);
    }

    @Test
    void shouldGenerateCollections() {
        var generator = new Generator();

        List<?> list = (List<?>) generator.generateValueOfType(List.class);
        assertNotNull(list);

        Set<?> set = (Set<?>) generator.generateValueOfType(Set.class);
        assertNotNull(set);
    }

    @Test
    void shouldGenerateCustomClass() {
        var generator = new Generator();
        var instance = generator.generateValueOfType(TestClass.class);
        assertNotNull(instance);
    }

    @Timeout(60)
    @Test
    void shouldStopGenerationEvenWithCyclicalDependencies() {
        var generator = new Generator();
        var instance = generator.generateValueOfType(CyclicClassA.class);
        assertNotNull(instance);
    }

    @Test
    void shouldGenerateClassWithFieldOfAnotherCustomClass() {
        var generator = new Generator();
        var instance = generator.generateValueOfType(DeepClass.class);
        assertNotNull(instance);
    }
}