package be.klak.rhino;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import be.klak.rhino.RhinoContext;

public class RhinoContextClassExportingTest {

    @Test
    public void exposingClassInJS() {
        RhinoContext context = new RhinoContext();

        ClassInJS newDefaultInstance = context.createClassInJS(ClassInJS.class);
        assertThat(newDefaultInstance.jsGet_prop()).isEqualTo(0);

        ClassInJS objInJava = (ClassInJS) context.evalJS("var obj = new ClassInJS(); obj");

        objInJava.increaseProp();
        assertThat(newDefaultInstance.jsGet_prop()).isEqualTo(0);
        assertThat(context.evalJS("obj.prop")).isEqualTo(1);
        assertThat(context.evalJS("obj.fn()")).isEqualTo("fn");
    }
}
