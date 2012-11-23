package be.klak.junit.jasmine.classes;

import be.klak.junit.jasmine.JasmineSuite;

@JasmineSuite(specs = { "doesNotLoadEnvJSSpec.js" }, envJs = false)
public class JasmineTestRunnerDoesNotLoadEnvJS { }
