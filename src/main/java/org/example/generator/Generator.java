package org.example.generator;

import org.example.annotations.Generatable;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Supplier;

public class Generator {
    private static final int MAX_RECURSION_LEVEL = 3;
    private static final int DEFAULT_ITEMS_COUNT = 2;
    private static final String DEFAULT_PACKAGE_NAME = "org.example.classes";

    private final Random random;
    private final Map<Class<?>, Supplier<?>> valueSuppliers;
    private final Map<Class<?>, List<Class<?>>> interfaceImplementationCache;
    private final String scanPackageName;

    public Generator() {
        this(DEFAULT_PACKAGE_NAME);
    }

    public Generator(String scanPackageName) {
        this.random = new Random();
        this.valueSuppliers = initializeSuppliers();
        this.interfaceImplementationCache = new HashMap<>();
        this.scanPackageName = scanPackageName;
    }

    public Object generateValueOfType(Class<?> targetClass) {
        return generateInternal(targetClass, 0);
    }

    private Object generateInternal(Class<?> clazz, int level) {
        if (level > MAX_RECURSION_LEVEL) {
            return getSafeDefault(clazz);
        }

        if (!isBuiltInType(clazz) && !clazz.isAnnotationPresent(Generatable.class)) {
            throw new IllegalArgumentException("Type " + clazz.getName() + " requires @Generatable");
        }

        if (valueSuppliers.containsKey(clazz)) {
            return valueSuppliers.get(clazz).get();
        }

        if (clazz.isArray()) {
            return generateArray(clazz, level);
        }

        if (isCollection(clazz)) {
            return generateCollection(clazz, level);
        }

        if (isMap(clazz)) {
            return generateMap(clazz, level);
        }

        if (clazz.isEnum()) {
            Object[] values = clazz.getEnumConstants();
            return values.length > 0 ? values[random.nextInt(values.length)] : null;
        }

        if (clazz.isInterface()) {
            Class<?> implementation = findInterfaceImplementation(clazz);
            return generateInternal(implementation, level + 1);
        }

        return generateClassInstance(clazz, level);
    }

    private Object generateClassInstance(Class<?> clazz, int level) {
        try {
            Object instance = createByConstructor(clazz, level);
            fillFields(instance, clazz, level);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build instance of " + clazz.getName(), e);
        }
    }

    private Object createByConstructor(Class<?> clazz, int level) throws Exception {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                constructor.setAccessible(true);
                return constructor.newInstance();
            }
        }

        Constructor<?> constructor = constructors[0];
        constructor.setAccessible(true);

        Object[] parameters = Arrays.stream(constructor.getParameterTypes())
                .map(paramType -> generateInternal(paramType, level + 1))
                .toArray();

        return constructor.newInstance(parameters);
    }

    private void fillFields(Object instance, Class<?> clazz, int level) throws Exception {
        for (Field field : clazz.getDeclaredFields()) {
            if (shouldIgnore(field)) continue;

            field.setAccessible(true);
            Object value = createFieldValue(field, level);
            field.set(instance, value);
        }
    }

    private Object createFieldValue(Field field, int level) {
        Class<?> fieldType = field.getType();

        if (isCollection(fieldType) && field.getGenericType() instanceof ParameterizedType pt) {
            return buildGenericCollection(field, pt, level);
        }

        if (isMap(fieldType) && field.getGenericType() instanceof ParameterizedType pt) {
            return buildGenericMap(field, pt, level);
        }

        return generateInternal(fieldType, level + 1);
    }

    private Object buildGenericCollection(Field field, ParameterizedType pt, int level) {
        Collection<Object> collection = createCollection(field.getType());
        Type[] typeArgs = pt.getActualTypeArguments();

        if (typeArgs.length == 1 && typeArgs[0] instanceof Class<?> elementType) {
            for (int i = 0; i < DEFAULT_ITEMS_COUNT; i++) {
                collection.add(generateInternal(elementType, level + 1));
            }
        }

        return collection;
    }

    private Object buildGenericMap(Field field, ParameterizedType pt, int level) {
        Map<Object, Object> map = new HashMap<>();
        Type[] typeArgs = pt.getActualTypeArguments();

        if (typeArgs.length == 2 &&
                typeArgs[0] instanceof Class<?> keyType &&
                typeArgs[1] instanceof Class<?> valueType) {

            if (!isValidMapKey(keyType)) {
                throw new IllegalArgumentException("Invalid map key type: " + keyType.getName());
            }

            for (int i = 0; i < DEFAULT_ITEMS_COUNT; i++) {
                Object key = generateInternal(keyType, level + 1);
                Object value = generateInternal(valueType, level + 1);
                map.put(key, value);
            }
        }

        return map;
    }

    private Object generateArray(Class<?> arrayType, int level) {
        Class<?> componentType = arrayType.getComponentType();
        int length = random.nextInt(3) + 1;
        Object array = Array.newInstance(componentType, length);

        for (int i = 0; i < length; i++) {
            Array.set(array, i, generateInternal(componentType, level + 1));
        }

        return array;
    }

    private Object generateCollection(Class<?> collectionType, int level) {
        return createCollection(collectionType);
    }

    private Object generateMap(Class<?> mapType, int level) {
        return new HashMap<>();
    }

    private Collection<Object> createCollection(Class<?> collectionType) {
        if (Set.class.isAssignableFrom(collectionType)) {
            return new HashSet<>();
        } else {
            return new ArrayList<>();
        }
    }

    private boolean isBuiltInType(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz.isArray() ||
                isCollection(clazz) ||
                isMap(clazz) ||
                clazz.isEnum() ||
                valueSuppliers.containsKey(clazz) ||
                clazz.getName().startsWith("java.");
    }

    private boolean shouldIgnore(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers);
    }

    private boolean isCollection(Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }

    private boolean isMap(Class<?> clazz) {
        return Map.class.isAssignableFrom(clazz);
    }

    private boolean isValidMapKey(Class<?> keyType) {
        return keyType.isPrimitive() ||
                keyType.isEnum() ||
                keyType == String.class ||
                Number.class.isAssignableFrom(keyType);
    }

    private Object getSafeDefault(Class<?> clazz) {
        return valueSuppliers.containsKey(clazz) ? valueSuppliers.get(clazz).get() : null;
    }

    private Map<Class<?>, Supplier<?>> initializeSuppliers() {
        Map<Class<?>, Supplier<?>> suppliers = new HashMap<>();

        suppliers.put(int.class, () -> random.nextInt(1000));
        suppliers.put(Integer.class, () -> random.nextInt(1000));
        suppliers.put(long.class, () -> random.nextLong(1000));
        suppliers.put(Long.class, () -> random.nextLong(1000));
        suppliers.put(double.class, () -> random.nextDouble() * 1000);
        suppliers.put(Double.class, () -> random.nextDouble() * 1000);
        suppliers.put(float.class, () -> random.nextFloat() * 1000);
        suppliers.put(Float.class, () -> random.nextFloat() * 1000);
        suppliers.put(byte.class, () -> (byte) random.nextInt(128));
        suppliers.put(Byte.class, () -> (byte) random.nextInt(128));
        suppliers.put(short.class, () -> (short) random.nextInt(1000));
        suppliers.put(Short.class, () -> (short) random.nextInt(1000));

        suppliers.put(boolean.class, random::nextBoolean);
        suppliers.put(Boolean.class, random::nextBoolean);
        suppliers.put(char.class, () -> (char) (random.nextInt(47) + 't'));
        suppliers.put(Character.class, () -> (char) (random.nextInt(47) + 't'));

        suppliers.put(String.class, () -> {
            int length = random.nextInt(20) + 5;
            var builder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                builder.append((char) (random.nextInt(26) + 'a'));
            }
            return builder.toString();
        });

        return suppliers;
    }

    private Class<?> findInterfaceImplementation(Class<?> interfaceType) {
        List<Class<?>> implementations = interfaceImplementationCache.get(interfaceType);
        if (implementations == null) {
            implementations = ClassPathScanner.findImplementations(interfaceType, scanPackageName);
            interfaceImplementationCache.put(interfaceType, implementations);
        }

        if (implementations.isEmpty()) {
            throw new IllegalArgumentException(
                    "No @Generatable implementation found for interface: " + interfaceType.getName()
            );
        }

        return implementations.get(random.nextInt(implementations.size()));
    }
}