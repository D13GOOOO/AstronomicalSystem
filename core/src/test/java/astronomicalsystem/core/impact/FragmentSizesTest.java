package astronomicalsystem.core.impact;

import astronomicalsystem.core.body.Mass;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FragmentSizesTest {

    private static final int THREE = 3;

    @Test
    void debrisLighterThanTheSmallestFragmentIsDust() {
        FragmentSizes SUT = new FragmentSizes(new Mass(0.5), THREE);

        List<Mass> fragments = SUT.split(0.25);

        assertThat(fragments).isEmpty();
    }
}
