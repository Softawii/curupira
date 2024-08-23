package com.softawii.curupira.v2.integration;

/**
 * ContextProvider to map instances to the CurupiraMapper.
 * If you're using a dependency injection framework, you can implement this interface to provide the instances.
 * Like, for example, Create a Wrapper for your Spring Context and implement this interface to provide the instances.
 * If you want a basic implementation, you can use {@link BasicContextProvider}.
 *
 *  @param <T> the type of the instance
 *
 * @since 2.0.0
 */
public interface ContextProvider {
    <T> T getInstance(Class<T> var1);
}
