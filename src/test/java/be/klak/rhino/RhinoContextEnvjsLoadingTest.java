package be.klak.rhino;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.tools.shell.Global;

import be.klak.rhino.RhinoContext;

public class RhinoContextEnvjsLoadingTest {

    @Test
    public void loadEnvShouldSetWindowSpaceAndBeES5Complaint() {
        RhinoContext context = new RhinoContext();

        context.loadEnv("src/test/javascript");
        assertThat(context.evalJS("window")).isInstanceOf(Global.class);

        assertThat(context.evalJS("Object.create({ test: 'test' });")).isInstanceOf(NativeObject.class);
    }

    @Test(expected = EcmaError.class)
    public void failWithoutLoadingEnvAndManipulatingDOMStuff() {
        RhinoContext context = new RhinoContext();
        context.evalJS("document.getElementById");
    }

}
