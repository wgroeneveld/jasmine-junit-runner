package be.klak.junit.jasmine;

import be.klak.junit.resources.FileResource;
import org.apache.commons.io.IOUtils;

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

    private final List<FileResource> javascriptFiles;
    private final List<FileResource> cssFiles;

    public HtmlPageRunner(List<FileResource> javascriptFiles, List<FileResource> cssFiles){
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
        for (FileResource sourceFile : cssFiles) {
            sourceFileIncludes.append("\t\t<link rel=\"stylesheet\" type=\"text/css\" href=\"" +
                                           "file://" + sourceFile.getURL().getFile() + "\">\r\n");
        }
        return sourceFileIncludes.toString();
    }

    private String getJavascriptFileIncludes() {
        StringBuilder sourceFileIncludes = new StringBuilder();
        for (FileResource sourceFile : javascriptFiles) {
            sourceFileIncludes.append("\t\t<script type='text/javascript' src='"
                                        + "file://" + sourceFile.getURL().getFile() + "'></script>\r\n");
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
