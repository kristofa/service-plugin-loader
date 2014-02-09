package com.github.kristofa.servicepluginloader;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Encapsulates a collection of url's at which we can load classes that are specific for a Service Plugin extension.
 * <p/>
 * So it defines the classpath for a single service plugin.
 * 
 * @author kristof
 */
public class ServicePluginClasspath {

    private final Collection<URL> urls = new HashSet<URL>();

    /**
     * Create a new instance.
     * 
     * @param urls Urls at which to load classes for this plugin.
     */
    public ServicePluginClasspath(final Collection<URL> urls) {
        this.urls.addAll(urls);
    }

    /**
     * Get classpath urls for plugin.
     * 
     * @return Collection of classpath urls for plugin.
     */
    public Collection<URL> getUrls() {
        return Collections.unmodifiableCollection(urls);
    }

}
