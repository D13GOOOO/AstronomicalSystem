package astronomicalsystem.app.sky;

import astronomicalsystem.core.math.Vector3;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.random.RandomGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StarsTest {

    private static final double TOLERANCE = 1e-12;
    // Three quarters of the way up the axis of the sphere, from -1 to 1: half a radius above the
    // equator, where the parallel has radius sqrt(3) / 2
    private static final double THREE_QUARTERS_UP = 0.75;
    private static final double A_QUARTER_TURN = 0.25;
    private static final Vector3 HALF_WAY_UP_ON_THE_Y_SIDE = new Vector3(0.0, Math.sqrt(3.0) / 2.0, 0.5);
    // One star in eight is brighter than this one: (1 / 8)^(-2/3) = 4 times the faintest
    private static final double BRIGHTER_THAN_SEVEN_IN_EIGHT = 0.875;
    private static final double FOUR_TIMES_THE_FAINTEST = 4.0;

    private final RandomGenerator random = mock();

    @Test
    void aStarIsDrawnEvenlyOverTheSphereAndItsFluxFromTheCountsOfStars() {
        when(random.nextDouble()).thenReturn(THREE_QUARTERS_UP, A_QUARTER_TURN, BRIGHTER_THAN_SEVEN_IN_EIGHT);

        List<Star> stars = Stars.scattered(random, 1);

        assertThat(stars).singleElement().satisfies(star -> {
            assertThat(star.direction().minus(HALF_WAY_UP_ON_THE_Y_SIDE).length()).isLessThan(TOLERANCE);
            assertThat(star.flux()).isCloseTo(FOUR_TIMES_THE_FAINTEST, within(TOLERANCE));
        });
    }
}
