package be.klak.rhino;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;

import be.klak.rhino.RhinoContext;
import be.klak.rhino.RhinoRunnable;

public class RhinoContextTest {

    @Test
    public void executeFunctionOnPrototypeAndActualObject() {
        RhinoContext context = new RhinoContext();
        String js = "" +
                "var obj = function() {" +
                "  this.actual = function() { " +
                "    return 5; " +
                "  }" +
                "};" +
                "obj.prototype = {" +
                "   go : function() {" +
                "    return 3; " +
                "   }" +
                "}";

        context.evalJS(js);
        ScriptableObject obj = (ScriptableObject) context.evalJS("new obj()");

        assertThat(context.executeFunction(obj, "go")).isEqualTo(3.0);
        assertThat(context.executeFunction(obj, "actual")).isEqualTo(5.0);
    }

    @Test
    public void runAsyncUsesTheSameSharedGlobalScope() throws InterruptedException {
        RhinoContext baseContext = new RhinoContext();
        baseContext.evalJS("var base = 'base'");

        baseContext.runAsync(new RhinoRunnable() {

            @Override
            public void run(RhinoContext context) {
                assertThat(context.evalJS("base")).isEqualTo("base");
            }
        });

        Thread.sleep(500);
    }

    @Test(expected = IllegalStateException.class)
    public void setPropertyOnUndefinedNotPossible() {
        RhinoContext context = new RhinoContext();
        context.evalJS("var zever");
        context.setProperty("zever", "prop", 1);
    }

    @Test(expected = IllegalStateException.class)
    public void setPropertyOnNullNotPossible() {
        RhinoContext context = new RhinoContext();
        context.setProperty(null, null, null);
    }

    @Test
    public void setPropertyOnSomeObj() {
        RhinoContext context = new RhinoContext();

        NativeObject anObj = (NativeObject) context.evalJS("var obj = { a: 'a'}; obj");
        context.setProperty("obj", "b", "b");

        assertThat(anObj.get("b", anObj)).isEqualTo("b");
    }

    @Test
    public void loadMultipleJSFiles() {
        RhinoContext context = new RhinoContext();
        context.load("src/test/javascript/", "loadTest.js", "loadTestTwo.js");

        assertThat(context.evalJS("loaded")).isEqualTo(true);
        assertThat(context.evalJS("loadedTwo")).isEqualTo(true);
    }
}
