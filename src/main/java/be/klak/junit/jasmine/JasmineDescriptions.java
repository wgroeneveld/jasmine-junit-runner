package be.klak.junit.jasmine;

import java.util.Collection;

import org.junit.runner.Description;

class JasmineDescriptions {

	private final Description rootDescription;
	private final Collection<JasmineSpec> specs;

	public JasmineDescriptions(Description rootDescription, Collection<JasmineSpec> specs) {
		this.rootDescription = rootDescription;
		this.specs = specs;
	}

	public Description getRootDescription() {
		return rootDescription;
	}

	public Collection<JasmineSpec> getSpecs() {
		return specs;
	}
}
