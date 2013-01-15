package be.klak.junit.jasmine;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;
import java.io.IOException;


import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunNotifier;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import be.klak.junit.jasmine.JasmineTestRunner;
import be.klak.junit.jasmine.classes.JasmineSuiteGeneratorClassWithRunner;
import be.klak.junit.jasmine.classes.JasmineSuiteGeneratorClassWithRunnerInSubDir;
import be.klak.junit.jasmine.classes.JasmineSuiteGeneratorClassWithoutRunner;

@RunWith(MockitoJUnitRunner.class)
public class JasmineSuiteGeneratesRunnerTest {

    private static final String RUNNERS_OUTPUT_DIR = "src/test/javascript/runners/";
    @Mock
    private RunNotifier notifierMock;

    @Before
    public void clearRunnersOutputDirectory() throws IOException {
        FileUtils.cleanDirectory(new File(RUNNERS_OUTPUT_DIR));
    }

    @Test
    public void byDefaultDoNotGenerateJasmineTestRunner() {
        Class<JasmineSuiteGeneratorClassWithoutRunner> testClass = JasmineSuiteGeneratorClassWithoutRunner.class;
        new JasmineTestRunner(testClass).run(notifierMock);

        File runnerResult = getTestRunnerResultFile(testClass);
        assertThat(runnerResult.isFile()).isFalse();
    }

    @Test
    public void generateJasmineTestRunnerAfterRunningTests() throws IOException {
        Class<JasmineSuiteGeneratorClassWithRunner> testClass = JasmineSuiteGeneratorClassWithRunner.class;
        new JasmineTestRunner(testClass).run(notifierMock);

        File runnerResult = getTestRunnerResultFile(testClass);
        assertThat(runnerResult.isFile()).isTrue();

        String runnerContent = FileUtils.readFileToString(runnerResult);

        assertThat(runnerContent).contains("jasmine.getEnv().addReporter(new jasmine.TrivialReporter());");
        assertJSFileIncluded(runnerContent,
                "./../../../main/webapp/js/source1.js",
                "./../../../main/webapp/js/source2.js",
                "./../specs/spec1.js",
                "./../specs/spec2.js");
    }

    @Test
    public void generateJasmineTestRunnerAfterRunningTestsWithSubDir() throws IOException {
      Class<JasmineSuiteGeneratorClassWithRunnerInSubDir> testClass =
        JasmineSuiteGeneratorClassWithRunnerInSubDir.class;
      new JasmineTestRunner(testClass).run(notifierMock);

      File runnerResult = getTestRunnerResultFile(testClass, "subDir1/subDir2");
      assertThat(runnerResult.isFile()).isTrue();

      String runnerContent = FileUtils.readFileToString(runnerResult);

      assertThat(runnerContent).contains("jasmine.getEnv().addReporter(new jasmine.TrivialReporter());");
      assertJSFileIncluded(runnerContent,
              "./../../../main/webapp/js/source1.js",
              "./../../../main/webapp/js/source2.js",
              "./../specs/spec1.js",
              "./../specs/spec2.js");

    }

    private File getTestRunnerResultFile(Class<?> testClass) {
        return getTestRunnerResultFile(testClass, StringUtils.EMPTY);
    }

//    private File getTestRunnerResultFile(Class<?> testClass) {
//      return new File(RUNNERS_OUTPUT_DIR + testClass.getSimpleName() + "Runner.html");
//  }

    private File getTestRunnerResultFile(Class<?> testClass, String subDir) {
      StringBuffer filePath = new StringBuffer(RUNNERS_OUTPUT_DIR);
      if (StringUtils.isNotBlank(subDir)) {
        filePath.append(subDir).append('/');
      }
      filePath.append(testClass.getSimpleName()).append("Runner.html");
      return new File(filePath.toString());
    }

    private void assertJSFileIncluded(String rawContent, String... files) {
        for (String file : files) {
            assertThat(rawContent).contains("<script type='text/javascript' src='" + file + "'></script>");
        }
    }
}
