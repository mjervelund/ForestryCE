package forestry.gametest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@code public static void name(GameTestHelper)} method as a Forestry GameTest. 26.x dropped
 * {@code @GameTest}/{@code @GameTestHolder}: tests are registry entries now, so {@link ForestryGameTests} finds these
 * methods in the mod scan data and registers each as a test function plus a test instance in the {@code minecraft:empty}
 * structure, named {@code forestry:<class>.<method>} (lowercase), like the 1.21.1 holder did.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ForestryGameTest {
	/** Same default as 1.21.1's {@code @GameTest}. */
	int timeoutTicks() default 100;
}
