package be.klak.junit.jasmine;

import be.klak.junit.resources.ClasspathResource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
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
        List<File> javascriptFiles = new ArrayList<File>();

        List<ClasspathResource> resources = Arrays.asList(
            new ClasspathResource("js/lib/jasmine-1.0.2/jasmine.js"),
            new ClasspathResource("js/lib/jasmine-1.0.2/jasmine-html.js")
        );
        for(ClasspathResource resource : resources){
            File outputFile = new File(outputPath, resource.getBaseName());
            try {
                FileUtils.writeStringToFile(outputFile, IOUtils.toString(resource.getURL().openStream()));
                javascriptFiles.add(outputFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

		// TODO hardcoded relative path stuff wat configureerbaar maken
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
