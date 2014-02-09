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
    private Properties expectedPluginProperties;
    private Properties expectedPluginProperties2;

    @Before
    public void setup() {

        expectedPluginProperties = new Properties();
        expectedPluginProperties.setProperty("shape", "plus");
        expectedPluginProperties.setProperty("description", "Shape drawer that draws shapes using + sign.");

        expectedPluginProperties2 = new Properties();
        expectedPluginProperties2.setProperty("shape", "min");
        expectedPluginProperties2.setProperty("description", "Shape drawer that draws shapes using - sign.");

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

        matchSinglePlugin(expectedPluginProperties, expectedPluginProperties);
        matchSinglePlugin(expectedPluginProperties2, expectedPluginProperties2);
    }

    @Test
    public void testGetPartiallyMatchingProperties() {
        final Properties matchingProperties = new Properties();
        matchingProperties.setProperty("shape", "plus");
        matchSinglePlugin(matchingProperties, expectedPluginProperties);

        final Properties properties2 = new Properties();
        properties2.setProperty("shape", "min");
        matchSinglePlugin(properties2, expectedPluginProperties2);
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

    private void matchSinglePlugin(final Properties matchingProperties, final Properties allServicePluginProperties) {
        final Collection<ServicePlugin<ShapeDrawer>> plugins = pluginLoader.get(ShapeDrawer.class, matchingProperties);
        assertEquals(1, plugins.size());
        final ServicePlugin<ShapeDrawer> plugin1 = plugins.iterator().next();
        assertEquals(allServicePluginProperties, plugin1.getProperties());
        assertNotNull(plugin1.getPlugin());
    }
}
