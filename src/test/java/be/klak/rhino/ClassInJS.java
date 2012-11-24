package be.klak.rhino;

import org.mozilla.javascript.ScriptableObject;

public class ClassInJS extends ScriptableObject {

    private int prop = 0;

    public ClassInJS() {
    }

    public void increaseProp() {
        prop++;
    }

    public String jsFunction_fn() {
        return "fn";
    }

    public int jsGet_prop() {
        return prop;
    }

    @Override
    public String getClassName() {
        return "ClassInJS";
    }
}
