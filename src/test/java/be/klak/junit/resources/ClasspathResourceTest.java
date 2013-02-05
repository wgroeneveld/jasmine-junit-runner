package be.klak.junit.resources;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.MalformedURLException;
import java.net.URL;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClasspathResourceTest {
    @Mock ClassLoader classLoader;

    @Test
    public void shouldHaveABaseName() throws MalformedURLException {
        when(classLoader.getResource("foo/bar.js")).thenReturn(new URL("file:///foo/bar.js"));

        ClasspathResource resource = new ClasspathResource("foo/bar.js", classLoader);

        assertThat(resource.getBaseName()).isEqualTo("bar.js");
    }
}
