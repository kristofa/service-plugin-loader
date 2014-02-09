package com.github.kristofa.servicepluginloader;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.github.kristofa.servicepluginloader.example.ShapeDrawer;

public class ServicePluginLoaderTest {

    private ServicePluginLoader<ShapeDrawer> pluginLoader;

    @Before
    public void setup() {
        final ServicePluginsClassPathProvider pluginsClassPathProvider = new ServicePluginsClassPathProvider() {

            @Override
            public Collection<ServicePluginClasspath> getPlugins() {
                try {
                    final ServicePluginClasspath plugin1 =
                        new ServicePluginClasspath(Arrays.asList(new URL(
                            "file:src/test/resources/shapedrawer1-0.1-SNAPSHOT.jar")));
                    final ServicePluginClasspath plugin2 =
                        new ServicePluginClasspath(Arrays.asList(new URL(
                            "file:src/test/resources/shapedrawer2-0.1-SNAPSHOT.jar")));
                    return Arrays.asList(plugin1, plugin2);
                } catch (final MalformedURLException e) {
                    throw new IllegalStateException(e);
                }

            }
        };

        pluginLoader = new ServicePluginLoader<ShapeDrawer>(pluginsClassPathProvider);
    }

    @Test
    public void testGet() {
        final Properties properties = new Properties();
        properties.setProperty("shape", "plus");
        final Collection<ShapeDrawer> plugins = pluginLoader.get(ShapeDrawer.class, properties);
        assertEquals(1, plugins.size());
    }
}
