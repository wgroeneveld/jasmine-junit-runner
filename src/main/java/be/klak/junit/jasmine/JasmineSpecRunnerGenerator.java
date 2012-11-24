package be.klak.junit.jasmine;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

class JasmineSpecRunnerGenerator {

	private enum TemplatePlaceholders {
		RELATIVE_PATH("<!--RelativePath-->"),
		SOURCE_FILES_TO_INCLUDE("<!--SourceFileIncludes-->"),
		SPEC_FILES_TO_INCLUDE("<!--SpecFileIncludes-->");

		private final String placeholder;

		private TemplatePlaceholders(String placeholder) {
			this.placeholder = placeholder;
		}

		public String getPlaceholder() {
			return placeholder;
		}

	}

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
		String template = loadTemplate();
		template = replaceRelativePathsForLibs(template);
		template = template.replaceAll(TemplatePlaceholders.SOURCE_FILES_TO_INCLUDE.getPlaceholder(),
				getJavascriptFileIncludes("./../../../main/webapp/js", suite.sources()));
		template = template.replaceAll(TemplatePlaceholders.SPEC_FILES_TO_INCLUDE.getPlaceholder(),
				getJavascriptFileIncludes("./../specs", jasmineSpecs));

		try {
			FileUtils.writeStringToFile(new File(outputPath + "/" + outputFileName), template);
		} catch (IOException e) {
			throw new RuntimeException("unable to write spec runner contents to destination", e);
		}
	}

	private String replaceRelativePathsForLibs(String template) {
		return template.replaceAll(TemplatePlaceholders.RELATIVE_PATH.getPlaceholder(), suite.jsRootDir());
	}

	private String getJavascriptFileIncludes(String path, String[] jsFiles) {
		StringBuilder sourceFileIncludes = new StringBuilder();
		for (String sourceFile : jsFiles) {
			sourceFileIncludes.append("\t\t<script type='text/javascript' src='" + path + "/" + sourceFile
					+ "'></script>\r\n");
		}
		return sourceFileIncludes.toString();
	}

	private String loadTemplate() {
		try {
			return IOUtils.toString(
				Thread
					.currentThread()
					.getContextClassLoader()
					.getResourceAsStream("js/lib/specRunner.tpl")
			);
		} catch (NullPointerException e) {
			throw new IllegalStateException("spec runner template file not found!");
		} catch (IOException e) {
			throw new IllegalStateException("spec runner template file could not be read!", e);
		}
	}
}
