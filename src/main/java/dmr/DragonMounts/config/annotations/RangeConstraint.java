package dmr.DragonMounts.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines range constraints for numeric configuration values.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RangeConstraint {
    /**
     * The minimum value.
     */
    double min() default Double.MIN_VALUE;

    /**
     * The maximum value.
     */
    double max() default Double.MAX_VALUE;
}
