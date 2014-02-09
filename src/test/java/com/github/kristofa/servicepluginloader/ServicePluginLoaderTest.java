package com.github.kristofa.servicepluginloader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
            public Collection<ServicePluginClassPath> getPlugins() {
                try {
                    final ServicePluginClassPath plugin1 =
                        new ServicePluginClassPath(new URL("file:src/test/resources/shapedrawer1-0.1-SNAPSHOT.jar"));
                    final ServicePluginClassPath plugin2 =
                        new ServicePluginClassPath(new URL("file:src/test/resources/shapedrawer2-0.1-SNAPSHOT.jar"));
                    return Arrays.asList(plugin1, plugin2);
                } catch (final MalformedURLException e) {
                    throw new IllegalStateException(e);
                }

            }
        };

        pluginLoader = new ServicePluginLoader<ShapeDrawer>(pluginsClassPathProvider);
    }

    @Test
    public void testGetExactMatchingProperties() {
        final Properties properties = new Properties();
        properties.setProperty("shape", "plus");
        final Collection<ShapeDrawer> plugins = pluginLoader.get(ShapeDrawer.class, properties);
        assertEquals(1, plugins.size());

        final Properties properties2 = new Properties();
        properties2.setProperty("shape", "min");
        final Collection<ShapeDrawer> plugins2 = pluginLoader.get(ShapeDrawer.class, properties2);
        assertEquals(1, plugins2.size());
    }

    @Test
    public void testGetNoMatchingProperties() {
        final Properties properties = new Properties();
        properties.setProperty("shape", "hashtag");
        final Collection<ShapeDrawer> plugins = pluginLoader.get(ShapeDrawer.class, properties);
        assertTrue(plugins.isEmpty());
    }
}
