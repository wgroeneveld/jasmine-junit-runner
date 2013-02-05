package be.klak.junit.jasmine;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class JasmineSpecRunnerGenerator {

    private final JasmineSuite suite;
	private final String[] jasmineSpecs;
	private final String outputPath;
	private final String outputFileName;

	public JasmineSpecRunnerGenerator(String[] jasmineSpecs, JasmineSuite suite, String outputPath, String outputFileName) {
		this.jasmineSpecs = jasmineSpecs;
		this.suite = suite;
		this.outputPath = outputPath;
		this.outputFileName = outputFileName;
	}

	public void generate() {
		// TODO hardcoded relative path stuff wat configureerbaar maken
        List<File> javascriptFiles = new ArrayList<File>();
        for(String source : suite.sources()){
            javascriptFiles.add(new File(suite.sourcesRootDir(), source));
        }
        for(String spec : jasmineSpecs){
            javascriptFiles.add(new File(new File(suite.jsRootDir(), "specs"), spec));
        }

        HtmlPageRunner htmlPageRunner = new HtmlPageRunner(javascriptFiles);
        try {
			FileUtils.writeStringToFile(new File(outputPath + "/" + outputFileName), htmlPageRunner.render());
		} catch (IOException e) {
			throw new RuntimeException("unable to write spec runner contents to destination", e);
		}
	}
}
