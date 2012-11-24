package be.klak.junit.jasmine;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.Description;

import be.klak.junit.jasmine.classes.JasmineSuiteGeneratorClassWithRunner;

public class DescriptionsWithMultipleSpecFilesTest {

	@Test
	public void getDescriptionsShouldIncludeBothSpec1AndSpec2SuiteInfo() {
		Description root = new JasmineTestRunner(JasmineSuiteGeneratorClassWithRunner.class).getDescription();

		assertThat(root.getChildren()).hasSize(2);
		Description spec1 = root.getChildren().get(0);
		assertThat(spec1.getDisplayName()).isEqualTo("spec 1");
		assertThat(spec1.getChildren()).hasSize(1);

		Description spec2 = root.getChildren().get(1);
		assertThat(spec2.getDisplayName()).isEqualTo("spec 2");
		assertThat(spec2.getChildren()).hasSize(1);
	}

}
