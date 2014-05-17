package cz.sortivo.reporting.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface StatField {
    String function() default "none";
    boolean ignored() default false;
    boolean groupBy() default false;
    

}
