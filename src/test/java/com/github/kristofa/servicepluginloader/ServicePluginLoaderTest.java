package com.github.kristofa.servicepluginloader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
    public void testGetAllForType() {
        final Collection<ServicePlugin<ShapeDrawer>> plugins = pluginLoader.get(ShapeDrawer.class);
        assertEquals(2, plugins.size());

    }

    @Test
    public void testGetExactlyMatchingProperties() {
        final Properties properties = new Properties();
        properties.setProperty("shape", "plus");
        properties.setProperty("description", "Shape drawer that draws shapes using + sign.");
        final Collection<ServicePlugin<ShapeDrawer>> plugins = pluginLoader.get(ShapeDrawer.class, properties);
        assertEquals(1, plugins.size());
        final ServicePlugin<ShapeDrawer> plugin1 = plugins.iterator().next();
        assertEquals(properties, plugin1.getProperties());
        assertNotNull(plugin1.getPlugin());

        final Properties properties2 = new Properties();
        properties2.setProperty("shape", "min");
        properties2.setProperty("description", "Shape drawer that draws shapes using - sign.");
        final Collection<ServicePlugin<ShapeDrawer>> plugins2 = pluginLoader.get(ShapeDrawer.class, properties2);
        assertEquals(1, plugins2.size());
        final ServicePlugin<ShapeDrawer> plugin2 = plugins2.iterator().next();
        assertEquals(properties2, plugin2.getProperties());
        assertNotNull(plugin2.getPlugin());
    }

    @Test
    public void testGetPartiallyMatchingProperties() {
        final Properties properties = new Properties();
        properties.setProperty("shape", "plus");
        final Collection<ServicePlugin<ShapeDrawer>> plugins = pluginLoader.get(ShapeDrawer.class, properties);
        assertEquals(1, plugins.size());
        final ServicePlugin<ShapeDrawer> plugin1 = plugins.iterator().next();
        final Properties expectedProperties1 = new Properties();
        expectedProperties1.setProperty("shape", "plus");
        expectedProperties1.setProperty("description", "Shape drawer that draws shapes using + sign.");
        assertEquals(expectedProperties1, plugin1.getProperties());
        assertNotNull(plugin1.getPlugin());

        final Properties properties2 = new Properties();
        properties2.setProperty("shape", "min");
        final Collection<ServicePlugin<ShapeDrawer>> plugins2 = pluginLoader.get(ShapeDrawer.class, properties2);
        assertEquals(1, plugins2.size());
    }

    @Test
    public void testGetNoMatchingProperties() {
        final Properties properties = new Properties();
        properties.setProperty("shape", "hashtag");
        final Collection<ServicePlugin<ShapeDrawer>> plugins = pluginLoader.get(ShapeDrawer.class, properties);
        assertTrue(plugins.isEmpty());
    }

    @Test
    public void testGetNoPropertiesSpecified() {
        final Properties properties = new Properties();
        final Collection<ServicePlugin<ShapeDrawer>> plugins = pluginLoader.get(ShapeDrawer.class, properties);
        assertEquals(2, plugins.size());

    }
}
