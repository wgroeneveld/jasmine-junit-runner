package be.klak.junit.resources;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class FileResource implements Resource {
    private final File file;

    public FileResource(String path){
        this(new File(path));
    }

    public FileResource(File file){
        this.file = file;
    }

    public URL getURL(){
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getBaseName() {
        String external = getURL().toExternalForm();
        return external.substring(external.lastIndexOf("/") + 1);
    }

    public static FileResource from(Resource initial, File output) {
        File outputFile = new File(output, initial.getBaseName());
        try {
            FileUtils.writeStringToFile(outputFile, IOUtils.toString(initial.getURL().openStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new FileResource(outputFile);
    }
}
