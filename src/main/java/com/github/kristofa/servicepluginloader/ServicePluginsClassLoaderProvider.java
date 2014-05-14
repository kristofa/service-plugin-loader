package com.github.kristofa.servicepluginloader;

import java.util.Collection;

/**
 * Provides a classloader for each plug in. This should be a separate classloader per plug in to avoid classpath collisions.
 */
public interface ServicePluginsClassLoaderProvider {

    Collection<ClassLoader> getPluginsClassLoaders();

}
