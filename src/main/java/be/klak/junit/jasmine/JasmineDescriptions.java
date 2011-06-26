package be.klak.junit.jasmine;

import java.util.Collection;
import java.util.Map;


import org.junit.runner.Description;

import be.klak.rhino.RhinoContext;


class JasmineDescriptions {

    private final Description rootDescription;
    private final Map<String, JasmineSpec> specsMap;
    private final RhinoContext rhinoContext;

    JasmineDescriptions(Description rootDescription, Map<String, JasmineSpec> specsMap, RhinoContext context) {
        this.rootDescription = rootDescription;
        this.specsMap = specsMap;
        this.rhinoContext = context;
    }

    public Description getRootDescription() {
        return rootDescription;
    }

    public Collection<JasmineSpec> getAllSpecs() {
        return specsMap.values();
    }

    public void executeSpec(Description description) {
        getSpec(description).execute(rhinoContext);
    }

    public JasmineSpec getSpec(Description description) {
        return specsMap.get(description.getDisplayName());
    }

}
