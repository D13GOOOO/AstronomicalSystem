package astronomicalsystem.core.math;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.assertj.core.util.DoubleComparator;

/**
 * Comparisons of floating-point results shared by the tests.
 */
public final class Tolerances {

    private Tolerances() {
    }

    /**
     * @return a recursive comparison in which two doubles are equal when they differ by at most
     *         {@code tolerance}
     */
    public static RecursiveComparisonConfiguration closeEnough(double tolerance) {
        return RecursiveComparisonConfiguration.builder()
                .withComparatorForType(new DoubleComparator(tolerance), Double.class)
                .build();
    }
}
