package com.github.kristofa.servicepluginloader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.junit.After;
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
            public Collection<ServicePluginClasspath2> getPlugins() {
                try {
                    final ServicePluginClasspath2 plugin1 =
                        new ServicePluginClasspath2(new URL("file:src/test/resources/shapedrawer1-0.1-SNAPSHOT.jar"));
                    final ServicePluginClasspath2 plugin2 =
                        new ServicePluginClasspath2(new URL("file:src/test/resources/shapedrawer2-0.1-SNAPSHOT.jar"));
                    return Arrays.asList(plugin1, plugin2);
                } catch (final MalformedURLException e) {
                    throw new IllegalStateException(e);
                }

            }
        };

        pluginLoader = new ServicePluginLoader<ShapeDrawer>(ShapeDrawer.class, pluginsClassPathProvider);
    }

    @After
    public void tearDown() {
        pluginLoader.close();
    }

    @Test
    public void testLoadAll() {
        final Collection<ServicePlugin<ShapeDrawer>> plugins = pluginLoader.load();
        assertEquals(2, plugins.size());

    }

    @Test
    public void testLoadExactlyMatchingProperties() {

        matchSinglePlugin(expectedPluginProperties, expectedPluginProperties);
        matchSinglePlugin(expectedPluginProperties2, expectedPluginProperties2);
    }

    @Test
    public void testReload() {

        matchSinglePlugin(expectedPluginProperties, expectedPluginProperties);
        matchSinglePlugin(expectedPluginProperties2, expectedPluginProperties2);
        pluginLoader.reload();
        matchSinglePlugin(expectedPluginProperties, expectedPluginProperties);
        matchSinglePlugin(expectedPluginProperties2, expectedPluginProperties2);
    }

    @Test
    public void testLoadPartiallyMatchingProperties() {
        final Properties matchingProperties = new Properties();
        matchingProperties.setProperty("shape", "plus");
        matchSinglePlugin(matchingProperties, expectedPluginProperties);

        final Properties properties2 = new Properties();
        properties2.setProperty("shape", "min");
        matchSinglePlugin(properties2, expectedPluginProperties2);
    }

    @Test
    public void testLoadNoMatchingProperties() {
        final Properties properties = new Properties();
        properties.setProperty("shape", "hashtag");
        final Collection<ServicePlugin<ShapeDrawer>> plugins = pluginLoader.load(properties);
        assertTrue(plugins.isEmpty());
    }

    @Test
    public void testLoadNoPropertiesSpecified() {
        final Properties properties = new Properties();
        final Collection<ServicePlugin<ShapeDrawer>> plugins = pluginLoader.load(properties);
        assertEquals(2, plugins.size());

    }

    private void matchSinglePlugin(final Properties matchingProperties, final Properties allServicePluginProperties) {
        final Collection<ServicePlugin<ShapeDrawer>> plugins = pluginLoader.load(matchingProperties);
        assertEquals(1, plugins.size());
        final ServicePlugin<ShapeDrawer> plugin1 = plugins.iterator().next();
        assertEquals(allServicePluginProperties, plugin1.getProperties());
        assertNotNull(plugin1.getPlugin());
    }
}
