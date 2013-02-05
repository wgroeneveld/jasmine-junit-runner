package be.klak.junit.resources;

import java.net.URL;

public class ClasspathResource {
    private final String path;
    private final ClassLoader classLoader;

    public ClasspathResource(String path, ClassLoader classLoader){
        this.path = path;
        this.classLoader = classLoader;
    }

    public ClasspathResource(String path){
        this(path, Thread.currentThread().getContextClassLoader());
    }

    public URL getURL(){
        return classLoader.getResource(path);
    }

    public String getBaseName() {
        String external = getURL().toExternalForm();
        return external.substring(external.lastIndexOf("/") + 1);
    }
}
