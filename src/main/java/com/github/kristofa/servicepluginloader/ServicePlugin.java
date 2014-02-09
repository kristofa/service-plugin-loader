package com.github.kristofa.servicepluginloader;

import java.util.Properties;

import org.apache.commons.lang3.Validate;

/**
 * Represents a Service Plugin and its properties.
 * 
 * @author kristof
 * @param <T> Plugin type.
 */
public class ServicePlugin<T> {

    private final T plugin;
    private final Properties properties;

    ServicePlugin(final T plugin, final Properties properties) {
        Validate.notNull(plugin);
        Validate.notNull(properties);
        this.plugin = plugin;
        this.properties = properties;
    }

    public T getPlugin() {
        return plugin;
    }

    public Properties getProperties() {
        return properties;
    }
}
