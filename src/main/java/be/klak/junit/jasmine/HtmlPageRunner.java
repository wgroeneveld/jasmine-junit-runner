package be.klak.junit.jasmine;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

class HtmlPageRunner {
    private enum Placeholders {
        SOURCE_FILES_TO_INCLUDE("<!--SourceFileIncludes-->");

        private final String placeholder;

        private Placeholders(String placeholder) {
            this.placeholder = placeholder;
        }

        public String getPlaceholder() {
            return placeholder;
        }
    }

    private final List<File> javascriptFiles;

    public HtmlPageRunner(List<File> javascriptFiles){
        this.javascriptFiles = javascriptFiles;
    }

    public String render(){
        return loadTemplate().replace(Placeholders.SOURCE_FILES_TO_INCLUDE.getPlaceholder(), getJavascriptFileIncludes());
    }

    private String getJavascriptFileIncludes() {
        StringBuilder sourceFileIncludes = new StringBuilder();
        for (File sourceFile : javascriptFiles) {
            sourceFileIncludes.append("\t\t<script type='text/javascript' src='" + "file://" + sourceFile.getAbsolutePath()
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
