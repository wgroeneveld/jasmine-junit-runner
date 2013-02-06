package be.klak.junit.jasmine;

import be.klak.junit.resources.ClasspathResource;
import be.klak.junit.resources.FileResource;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class JasmineSpecRunnerGenerator {

    private final JasmineSuite suite;
	private final String[] jasmineSpecs;
	private final String outputPath;
	private final String outputFileName;
    private File outputFile;

    public JasmineSpecRunnerGenerator(String[] jasmineSpecs, JasmineSuite suite, String outputPath, String outputFileName) {
		this.jasmineSpecs = jasmineSpecs;
		this.suite = suite;
		this.outputPath = outputPath;
		this.outputFileName = outputFileName;
        this.outputFile = new File(outputPath);
	}

	public void generate() {
        List<FileResource> javascriptFiles = new ArrayList<FileResource>();

        javascriptFiles.addAll(Arrays.asList(
                FileResource.from(new ClasspathResource("js/lib/jasmine-1.0.2/jasmine.js"), outputFile),
                FileResource.from(new ClasspathResource("js/lib/jasmine-1.0.2/jasmine-html.js"), outputFile)
        ));
        for(String source : suite.sources()){
            javascriptFiles.add(new FileResource(new File(suite.sourcesRootDir(), source)));
        }
        for(String spec : jasmineSpecs){
            javascriptFiles.add(new FileResource(new File(new File(suite.jsRootDir(), "specs"), spec)));
        }

        List<FileResource> cssFiles = new ArrayList<FileResource>();
        cssFiles.add(FileResource.from(new ClasspathResource("js/lib/jasmine-1.0.2/jasmine.css"), outputFile));

        HtmlPageRunner htmlPageRunner = new HtmlPageRunner(javascriptFiles, cssFiles);
        try {
			FileUtils.writeStringToFile(new File(outputPath, outputFileName), htmlPageRunner.render());
		} catch (IOException e) {
			throw new RuntimeException("unable to write spec runner contents to destination", e);
		}
	}

}
