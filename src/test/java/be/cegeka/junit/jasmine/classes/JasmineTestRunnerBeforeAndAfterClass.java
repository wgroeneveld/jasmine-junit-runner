package be.cegeka.junit.jasmine.classes;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;

import be.cegeka.junit.jasmine.JasmineSuite;
import be.cegeka.rhino.RhinoContext;

@JasmineSuite(specs = { "emptySpec.js" })
public class JasmineTestRunnerBeforeAndAfterClass {

	private static final int RUN_MIJ_FLAG = 0;
	private static final int RUN_MIJ_OOK_FLAG = 1;

	List<Integer> runs = new ArrayList<Integer>();

	@Before
	public void runMij() {
		assertThat(runs).isEmpty();
		runs.add(RUN_MIJ_FLAG);
	}

	@Before
	public void runMijOok(RhinoContext context) {
		assertThat(runs).containsOnly(RUN_MIJ_FLAG);
		runs.add(RUN_MIJ_OOK_FLAG);
	}

	@After
	public void runMijAfter() {
		assertThat(runs).containsOnly(RUN_MIJ_FLAG, RUN_MIJ_OOK_FLAG);
		runs.remove((Object) RUN_MIJ_FLAG);
	}

	@After
	public void runMijAfterOok(RhinoContext context) {
		assertThat(runs).containsOnly(RUN_MIJ_OOK_FLAG);
		runs.remove((Object) RUN_MIJ_OOK_FLAG);
	}

	public void runMijNiet() {
		fail("should not be run");
	}
}
