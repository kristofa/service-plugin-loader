package com.github.kristofa.servicepluginloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;

public class DefaultClassLoaderProvider implements ServicePluginsClassLoaderProvider {

    private final ServicePluginsClassPathProvider pluginsClassPathProvider;

    public DefaultClassLoaderProvider(final ServicePluginsClassPathProvider pluginsClassPathProvider) {
        super();
        this.pluginsClassPathProvider = pluginsClassPathProvider;
    }

    @Override
    public Collection<ClassLoader> getPluginsClassLoaders() {
        final Collection<ClassLoader> result = new ArrayList<ClassLoader>();
        for (final ServicePluginClassPath extension : pluginsClassPathProvider.getPlugins()) {

            result.add(new URLClassLoader(extension.getUrls().toArray(new URL[0])));
        }
        return result;
    }
}
