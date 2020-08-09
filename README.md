# Service Plugin Loader #

**The repository is archived as it is not maintained anymore. It is still available in read-only mode and can still be forked.** 

Service Plugin Loader builds upon `java.util.ServiceLoader`.

It is used to discover and load service plugins that are extensions/plugins to your application.

Each plugin will have its own ClassLoader and classpath. In this way we avoid classpath collisions between
plugins. The services are loaded from external resources (local jar files, directories, over the network) that are initially not
part of the classpath of our application.

![ServicePluginLoader overview](https://raw.githubusercontent.com/kristofa/service-plugin-loader/master/src/main/resources/service_plugin_loader.png)

1. The application accesses the ServicePluginLoader, asking to get plugins that implement a specific interface and optionally with specific key/value properties.
2. The ServicePluginLoader consults a configuration which contains the classpath for each plugin. This is hidden behind an interface so we can choose to have the configuration in a file, retrieve it from a database, url,...
3. The ServicePluginLoader uses the `java.util.ServiceLoader` to find services in every defined plugin. For each plugin a separate classloader is used.  This means we won't end up with classpath collisions.
4. To avoid class path collisions the plugins can't be initially part of our application class path. The jars or directories with classes can come from anywhere as long as it can be defined by a URL.
5. The ServicePluginLoader has detected and loaded the plugins and will return the plugins the requester is interested in.  The plugins will remain cached which means that a 2nd request for services will not trigger a new load.

## Maven artefact ##

Available through Maven Central:

    <dependency>
        <groupId>com.github.kristofa</groupId>
        <artifactId>service-plugin-loader</artifactId>
        <version>0.1</version>
    </dependency>

## Creating plugins ##

Example plugin content:

    kristofs-mbp:resources adriaens$ jar tvf shapedrawer1-0.1-SNAPSHOT.jar
         0 Sun Feb 09 17:31:26 CET 2014 META-INF/
       127 Sun Feb 09 17:31:24 CET 2014 META-INF/MANIFEST.MF
         0 Sun Feb 09 17:31:26 CET 2014 com/
         0 Sun Feb 09 17:31:26 CET 2014 com/github/
         0 Sun Feb 09 17:31:26 CET 2014 com/github/kristofa/
         0 Sun Feb 09 17:31:26 CET 2014 com/github/kristofa/servicepluginloader/
         0 Sun Feb 09 17:31:26 CET 2014 com/github/kristofa/servicepluginloader/example/
         0 Sun Feb 09 17:31:26 CET 2014 META-INF/services/
       722 Sun Feb 09 17:31:26 CET 2014 com/github/kristofa/servicepluginloader/example/ShapeDrawerImpl.class
        64 Sun Feb 09 17:31:26 CET 2014 META-INF/services/com.github.kristofa.servicepluginloader.example.ShapeDrawer
        69 Sun Feb 09 17:31:26 CET 2014 META-INF/services/com.github.kristofa.servicepluginloader.example.ShapeDrawerImpl.properties
         0 Sun Feb 09 17:31:26 CET 2014 META-INF/maven/
         0 Sun Feb 09 17:31:26 CET 2014 META-INF/maven/com.github.kristofa/
         0 Sun Feb 09 17:31:26 CET 2014 META-INF/maven/com.github.kristofa/shapedrawer1/
       903 Mon Jan 27 16:02:52 CET 2014 META-INF/maven/com.github.kristofa/shapedrawer1/pom.xml
       123 Sun Feb 09 17:31:26 CET 2014 META-INF/maven/com.github.kristofa/shapedrawer1/pom.properties
    kristofs-mbp:resources adriaens$
    
Because we use `java.util.ServiceLoader` services need to be defined in a file that resides
in `META-INF/services/`.  In the example we have 1 service defined  in following file:


    64 Sun Feb 09 17:31:26 CET 2014 META-INF/services/com.github.kristofa.servicepluginloader.example.ShapeDrawer

The content of this file is:

    com.github.kristofa.servicepluginloader.example.ShapeDrawerImpl
    
This means we define a service that implements the interface `com.github.kristofa.servicepluginloader.example.ShapeDrawer`
and we have 1 implementation in our plugin: `com.github.kristofa.servicepluginloader.example.ShapeDrawerImpl`.

This is how `java.util.ServiceLoader` will find our service. 

Next to this `ServicePluginLoader` allows you to define properties (key/value pairs) for each service plugin implementation. 
These properties allow `ServicePluginLoader` to filter, query for the service plugins of your choice. 
This functionality is inspired by the properties support in [OSGI](http://www.osgi.org) Services.

The properties for the example plugin shown above are defined in:

    69 Sun Feb 09 17:31:26 CET 2014 META-INF/services/com.github.kristofa.servicepluginloader.example.ShapeDrawerImpl.properties

So the file containing the properties also needs to be in `META-INF/services/`, has the full name of the service class postponed with .properties
In this example the content of the file is:

    shape=plus
    description=Shape drawer that draws shapes using + sign.

So the file contains key=value entries that can be loaded using `java.util.Properties`.
