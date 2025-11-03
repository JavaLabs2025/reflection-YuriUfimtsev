package org.example.generator;

import org.example.annotations.Generatable;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ClassPathScanner {

    public static List<Class<?>> findImplementations(Class<?> interfaceType, String packageName) {
        List<Class<?>> implementations = new ArrayList<>();

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            URL resource = classLoader.getResource(path);

            if (resource != null && "file".equals(resource.getProtocol())) {
                scanDirectory(
                        new File(resource.toURI()),
                        packageName,
                        interfaceType,
                        implementations
                );
            }
        } catch (Exception e) {
            System.err.println("Failed to scan package " + packageName + " for " +
                    interfaceType.getName() + ": " + e.getMessage());
        }

        return implementations;
    }

    private static void scanDirectory(File directory, String packageName,
                                      Class<?> interfaceType, List<Class<?>> implementations) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName(),
                        interfaceType, implementations);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().replace(".class", "");
                processClassFile(className, interfaceType, implementations);
            }
        }
    }

    private static void processClassFile(String className, Class<?> interfaceType,
                                         List<Class<?>> implementations) {
        try {
            Class<?> clazz = Class.forName(className);

            if (interfaceType.isAssignableFrom(clazz) &&
                    !clazz.isInterface() &&
                    clazz.isAnnotationPresent(Generatable.class)) {
                implementations.add(clazz);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + className);
        } catch (NoClassDefFoundError e) {
            System.err.println("Cannot load class (missing dependencies): " + className);
        }
    }
}