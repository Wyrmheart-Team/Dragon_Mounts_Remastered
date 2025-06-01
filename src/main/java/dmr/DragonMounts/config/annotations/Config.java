package dmr.DragonMounts.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field as a configuration value.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Config {
    /**
     * The configuration key.
     */
    String key();

    /**
     * The configuration comment.
     */
    String[] comment() default {};

    /**
     * The translation key for this config.
     */
    String translation() default "";

    /**
     * Whether this config requires a world restart to take effect.
     */
    boolean worldRestart() default false;

    /**
     * The configuration category.
     * Can be a single string or an array for nested categories.
     */
    String[] category() default {};
}
