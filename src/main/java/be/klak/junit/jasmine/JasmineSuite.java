package be.klak.junit.jasmine;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.apache.commons.lang.StringUtils;

@Retention(RetentionPolicy.RUNTIME)
public @interface JasmineSuite {

    String jsRootDir() default "src/test/javascript";

    String sourcesRootDir() default "src/main/webapp/js";

    String[] specs() default {};

    String[] sources() default {};

    boolean generateSpecRunner() default false;

    boolean debug() default false;

    /**
     * If set to false, does not load EnvJS support.
     */
    boolean envJs() default true;

    String specRunnerSubDir() default StringUtils.EMPTY;
}
