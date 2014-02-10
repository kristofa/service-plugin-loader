package com.github.kristofa.servicepluginloader;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ServicePluginLoader} is used to discover and load services that are extensions/plugins to your application.
 * <p/>
 * Each plugin will have its own {@link ClassLoader} and classpath. In this way we avoid classpath collisions between
 * plugins. The services are loaded from external resources (jar files, directories, over the network) that are initially not
 * part of the classpath of our application.
 * <p/>
 * This {@link ServicePluginLoader} uses the {@link java.util.ServiceLoader} underneath but extends it with the possibility
 * to define custom queryable properties for services.
 * <p/>
 * Services are discovered and loaded lazily when calling any of the load methods for the first time. Once loaded they are
 * cached till close() or reload() is called.
 * 
 * @author kristof
 * @param <T> The type of services we want to discover and load.
 */
public class ServicePluginLoader<T> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ServicePluginLoader.class);
    private final static Properties EMPTY_PROPERTIES = new Properties();
    private final Map<Properties, Collection<ServicePlugin<T>>> serviceMap =
        new HashMap<Properties, Collection<ServicePlugin<T>>>();
    private final ServicePluginsClassPathProvider pluginsClassPathProvider;
    private final Class<T> clazz;
    private boolean initialized = false;

    /**
     * Creates a new instance.
     * 
     * @param clazz Service Plugin type.
     * @param pluginProvider Provides the classpath for each of our plugins. Should not be <code>null</code>.
     */
    public ServicePluginLoader(final Class<T> clazz, final ServicePluginsClassPathProvider pluginProvider) {
        Validate.notNull(clazz);
        Validate.notNull(pluginProvider);
        this.clazz = clazz;
        this.pluginsClassPathProvider = pluginProvider;
    }

    /**
     * Discovers and loads plugins without filtering on Properties.
     * 
     * @param clazz Plugin type. Should not be <code>null</code>.
     * @return Collection of plugins. Collection can be empty in case we can't find plugins for given type.
     */
    public Collection<ServicePlugin<T>> load() {
        return load(EMPTY_PROPERTIES);
    }

    /**
     * Discovers and loads plugins.
     * 
     * @param clazz Plugin type. Should not be <code>null</code>.
     * @param properties Additional filter on plugins. Should not be <code>null</code>. A plugin will be returned if it has
     *            all given properties set. If these properties are a subset of the properties defined by the plugin it will
     *            also be returned. If empty properties object is passed in all plugins will be returned.
     * @return Collection of plugins. Collection can be empty in case we can't find matching plugins.
     */
    public Collection<ServicePlugin<T>> load(final Properties properties) {
        Validate.notNull(clazz);
        Validate.notNull(properties);
        synchronized (this) {
            if (!initialized) {
                try {
                    load(clazz);
                } catch (final MalformedURLException e) {
                    throw new IllegalStateException(e);
                }
                initialized = true;
            }
        }
        final Collection<ServicePlugin<T>> collection = serviceMap.get(properties);
        if (collection != null) {
            return Collections.unmodifiableCollection(collection);
        }
        // See if the properties we want are a subset of 1 or more plugins.
        return matchSubset(properties);
    }

    /**
     * Removes all loaded plugins. In case plugins implement {@link Closeable} interface they will be closed prior to
     * removal.
     * <p/>
     * Plugins, including any new added ones, will be loaded when calling any of the load methods.
     */
    public void reload() {
        close();
    }

    /**
     * Removes all loaded plugins. In case plugins implement {@link Closeable} interface they will be closed prior to
     * removal.
     * <p/>
     * If you call any of the load methods after close method service plugins will be reinitialized.
     */
    public void close() {

        for (final Collection<ServicePlugin<T>> collection : serviceMap.values()) {
            for (final ServicePlugin<T> servicePlugin : collection) {
                final T plugin = servicePlugin.getPlugin();
                if (plugin instanceof Closeable) {
                    try {
                        ((Closeable)plugin).close();
                    } catch (final IOException e) {
                        LOGGER.warn("Failed to close plugin.", e);
                    }
                } else {
                    break;
                }
            }
        }
        serviceMap.clear();
        initialized = false;

    }

    private Collection<ServicePlugin<T>> matchSubset(final Properties wantedProperties) {
        final Collection<ServicePlugin<T>> servicePlugins = new ArrayList<ServicePlugin<T>>();
        for (final Properties pluginProps : serviceMap.keySet()) {
            boolean match = true;
            for (final Object wantedKey : wantedProperties.keySet()) {
                final String foundPropertyValue = pluginProps.getProperty(wantedKey.toString());
                if (foundPropertyValue == null) {
                    match = false;
                    break;
                }
                final String wantedValue = wantedProperties.getProperty(wantedKey.toString());
                if (!wantedValue.equals(foundPropertyValue)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                servicePlugins.addAll(serviceMap.get(pluginProps));
            }
        }
        return servicePlugins;
    }

    private void load(final Class<T> clazz) throws MalformedURLException {

        for (final ServicePluginClassPath extension : pluginsClassPathProvider.getPlugins()) {

            final URL[] urls = getArray(extension);
            final URLClassLoader urlClassLoader = new URLClassLoader(urls);
            final java.util.ServiceLoader<T> loader = java.util.ServiceLoader.load(clazz, urlClassLoader);
            final Iterator<T> iterator = loader.iterator();
            while (iterator.hasNext()) {
                final T instance = iterator.next();
                final Properties properties = loadPropertiesForClass(urlClassLoader, instance.getClass().getName());

                Collection<ServicePlugin<T>> collection = serviceMap.get(properties);
                if (collection == null) {
                    collection = new ArrayList<ServicePlugin<T>>();
                    serviceMap.put(properties, collection);
                }
                collection.add(new ServicePlugin<T>(instance, properties));
            }
        }

    }

    private URL[] getArray(final ServicePluginClassPath extension) {
        final URL[] urls = new URL[extension.getUrls().size()];

        int i = 0;
        for (final URL url : extension.getUrls()) {
            urls[i] = url;
            i++;
        }
        return urls;
    }

    private Properties loadPropertiesForClass(final ClassLoader classLoader, final String className) {
        final Properties properties = new Properties();
        final String propertiesFileName = "META-INF/services/" + className + ".properties";
        final InputStream resourceAsStream = classLoader.getResourceAsStream(propertiesFileName);
        if (resourceAsStream == null) {
            return properties;
        }
        try {
            properties.load(resourceAsStream);
        } catch (final IOException e) {
            throw new IllegalStateException("Error when loading " + propertiesFileName, e);
        }
        return properties;
    }

}
