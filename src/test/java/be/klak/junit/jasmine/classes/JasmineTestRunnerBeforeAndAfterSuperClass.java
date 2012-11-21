package be.klak.junit.jasmine.classes;

import static org.fest.assertions.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;


import org.junit.Before;

import be.klak.rhino.RhinoContext;


public class JasmineTestRunnerBeforeAndAfterSuperClass {

    static final int RUN_MIJ_FLAG = 0;
    static final int RUN_MIJ_OOK_FLAG = 1;

    protected List<Integer> runs = new ArrayList<Integer>();

    @Before
    public void runMijOok(RhinoContext context) {
        assertThat(runs).containsOnly(RUN_MIJ_FLAG);
        runs.add(RUN_MIJ_OOK_FLAG);
    }

}
