package be.cegeka.junit.jasmine;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.junit.runner.Description;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;

import be.cegeka.rhino.RhinoContext;

class JasmineJSSuiteConverter {

    private final NativeArray baseSuites;
    private final RhinoContext context;

    JasmineJSSuiteConverter(NativeArray baseSuites, RhinoContext context) {
        this.baseSuites = baseSuites;
        this.context = context;
    }

    public JasmineDescriptions convertToJunitDescriptions(Class<?> testClass) {
        Description rootDescription = Description.createSuiteDescription(testClass);
        Map<String, JasmineSpec> specsMap = convertSuiteArrayToDescriptions(this.baseSuites, rootDescription);
        return new JasmineDescriptions(rootDescription, specsMap, context);
    }

    private Map<String, JasmineSpec> convertSuiteArrayToDescriptions(NativeArray suiteArray, Description rootDescription) {
        Map<String, JasmineSpec> specsMap = new HashMap<String, JasmineSpec>();
        for (Object idObj : suiteArray.getIds()) {
            NativeObject suite = (NativeObject) suiteArray.get((Integer) idObj, suiteArray);

            Description suiteDescription = Description
                    .createSuiteDescription((String) suite.get("description", suite), (Annotation[]) null);
            rootDescription.addChild(suiteDescription);
            specsMap.putAll(convertToJunitDescription(suite, suiteDescription));
        }

        return specsMap;
    }

    private Map<String, JasmineSpec> convertToJunitDescription(NativeObject suite, Description description) {
        Map<String, JasmineSpec> specsMap = new HashMap<String, JasmineSpec>();
        NativeArray specsArray = (NativeArray) context.executeFunction(suite, "specs");
        for (Object idObj : specsArray.getIds()) {
            NativeObject spec = (NativeObject) specsArray.get((Integer) idObj, specsArray);

            JasmineSpec jasmineSpec = new JasmineSpec(spec);
            specsMap.put(jasmineSpec.toString(), jasmineSpec);
            description.addChild(jasmineSpec.getDescription());
        }

        return specsMap;
    }

}
