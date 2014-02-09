package com.github.kristofa.servicepluginloader;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.lang3.Validate;

/**
 * Encapsulates a collection of url's at which we can load classes that are specific for a Service Plugin extension.
 * <p/>
 * So it defines the classpath for a single service plugin.
 * 
 * @author kristof
 */
public class ServicePluginClassPath {

    private final Collection<URL> urls = new HashSet<URL>();

    /**
     * Create a new instance.
     * 
     * @param urls Urls at which to load classes for this plugin. Collection should not be <code>null</code> and not be
     *            empty.
     */
    public ServicePluginClassPath(final Collection<URL> urls) {
        Validate.notNull(urls);
        Validate.isTrue(!urls.isEmpty(), "Collection should not be empty.");
        this.urls.addAll(urls);
    }

    /**
     * Create a new instance. Can be used in case classpath for plugin is single url.
     * 
     * @param url Single URL.
     */
    public ServicePluginClassPath(final URL url) {
        Validate.notNull(url);
        urls.add(url);
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
