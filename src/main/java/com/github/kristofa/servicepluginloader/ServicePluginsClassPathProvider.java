package com.github.kristofa.servicepluginloader;

import java.util.Collection;

/**
 * Provides the classpath for each of the plugins of our application.
 * 
 * @author kristof
 */
public interface ServicePluginsClassPathProvider {

    /**
     * Gets classpaths for each of the plugins of our application.
     * 
     * @return Collection which contains 1 entry for each plugin. Each entry contains the classpath for a plugin.
     */
    Collection<ServicePluginClassPath> getPlugins();

}
