package be.klak.junit.jasmine;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

class HtmlPageRunner {
    private enum Placeholders {
        SOURCE_FILES_TO_INCLUDE("<!--SourceFileIncludes-->"),
        CSS_FILES_TO_INCLUDE("<!--CssFileIncludes-->");

        private final String placeholder;

        private Placeholders(String placeholder) {
            this.placeholder = placeholder;
        }

        public String getPlaceholder() {
            return placeholder;
        }
    }

    private final List<File> javascriptFiles;
    private final List<File> cssFiles;

    public HtmlPageRunner(List<File> javascriptFiles, List<File> cssFiles){
        this.javascriptFiles = javascriptFiles;
        this.cssFiles = cssFiles;
    }

    public String render(){
        return loadTemplate()
                .replace(Placeholders.SOURCE_FILES_TO_INCLUDE.getPlaceholder(), getJavascriptFileIncludes())
                .replace(Placeholders.CSS_FILES_TO_INCLUDE.getPlaceholder(), getCssFileIncludes());
    }

    private String getCssFileIncludes() {
        StringBuilder sourceFileIncludes = new StringBuilder();
        for (File sourceFile : cssFiles) {
            sourceFileIncludes.append("\t\t<link rel=\"stylesheet\" type=\"text/css\" href=\"" +
                                           "file://" + sourceFile.getAbsolutePath() + "\">\r\n");
        }
        return sourceFileIncludes.toString();
    }

    private String getJavascriptFileIncludes() {
        StringBuilder sourceFileIncludes = new StringBuilder();
        for (File sourceFile : javascriptFiles) {
            sourceFileIncludes.append("\t\t<script type='text/javascript' src='"
                                        + "file://" + sourceFile.getAbsolutePath() + "'></script>\r\n");
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
