package be.klak.junit.jasmine;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface JasmineSuite {

    String jsRootDir() default "src/test/javascript";

    String sourcesRootDir() default "src/main/webapp/js";

    String[] specs() default {};

    String[] sources() default {};

    boolean generateSpecRunner() default false;

    boolean debug() default false;
}
