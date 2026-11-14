package astronomicalsystem.core.impact;

import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.math.Tolerances;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FragmentSizesTest {

    private static final int THREE = 3;
    private static final int TEN = 10;
    private static final RecursiveComparisonConfiguration CLOSE_ENOUGH = Tolerances.closeEnough(1e-12);

    @Test
    void debrisLighterThanTheSmallestFragmentIsDust() {
        FragmentSizes SUT = new FragmentSizes(new Mass(0.5), THREE);

        List<Mass> fragments = SUT.split(0.25);

        assertThat(fragments).isEmpty();
    }

    // Fragment sizes follow a power law, N(>m) ~ m^-0.95 (Leinhardt and Stewart): the k-th largest
    // fragment has a mass proportional to k^(-1 / 0.95), and together they make the whole debris
    @Test
    void theDebrisBreaksIntoFragmentsOfDecreasingSize() {
        FragmentSizes SUT = new FragmentSizes(new Mass(0.01), THREE);

        List<Mass> fragments = SUT.split(10.0);

        assertThat(fragments).usingRecursiveComparison(CLOSE_ENOUGH).isEqualTo(List.of(
                new Mass(5.56577781491278), new Mass(2.683194689509864), new Mass(1.7510274955773557)));
    }

    // Three fragments would leave the smallest at 0.268, below 0.3
    @Test
    void thereAreOnlyAsManyFragmentsAsCanBeAtLeastTheSmallest() {
        FragmentSizes SUT = new FragmentSizes(new Mass(0.3), TEN);

        List<Mass> fragments = SUT.split(1.0);

        assertThat(fragments).usingRecursiveComparison(CLOSE_ENOUGH).isEqualTo(List.of(
                new Mass(0.674723768557686), new Mass(0.32527623144231405)));
    }
}
