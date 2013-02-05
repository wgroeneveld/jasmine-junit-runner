package be.klak.junit.resources;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import static org.fest.assertions.Assertions.assertThat;

public class FileResourceTest {
    @Test
    public void shouldHaveABaseName() throws MalformedURLException {
        FileResource resource = new FileResource("foo/bar.js");

        assertThat(resource.getBaseName()).isEqualTo("bar.js");
    }

    @Test
    public void shouldMakeANewFileResourceRelativeToBaseDirFromResource() throws IOException {
        File tempFile = File.createTempFile("test", "first");
        FileUtils.writeStringToFile(tempFile, "sample");
        FileResource initial = new FileResource(tempFile);
        File outputDirectory = File.createTempFile("test", "second");
        outputDirectory.delete();
        outputDirectory.mkdirs();

        FileResource actual = FileResource.from(initial, outputDirectory);
        assertThat(outputDirectory.list()).contains(actual.getBaseName());
    }
}
