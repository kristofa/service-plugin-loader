# Service Plugin Loader

Service Plugin Loader builds upon `java.util.ServiceLoader`.

It is used to discover and load services that are extensions/plugins to our application.

Each plugin will have its own ClassLoader and classpath. In this way we avoid classpath collisions between
plugins. The services are loaded from external resources (local jar files, directories, over the network) that are initially not
part of the classpath of our application.

