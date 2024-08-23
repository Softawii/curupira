package com.softawii.curupira.v2.integration;

import java.util.HashMap;
import java.util.Map;

/**
 * Basic implementation of {@link ContextProvider} that stores instances in a map.
 *
 *  If you're using a dependency injection framework, you can implement this interface to provide the instances.
 *  Like, for example, Create a Wrapper for your Spring Context and implement this interface to provide the instances.
 *
 * @since 2.0.0
 */
public class BasicContextProvider implements ContextProvider {

    private final Map<Class<?>, Object> instances;

    public BasicContextProvider() {
        this.instances = new HashMap<>();
    }

    public <T> void registerInstance(Class<T> clazz, T instance) {
        instances.put(clazz, instance);
    }

    @Override
    public <T> T getInstance(Class<T> clazz) {
        return clazz.cast(instances.get(clazz));
    }
}
