package astronomicalsystem.core.rings;

import astronomicalsystem.core.body.Radius;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class RingBandTest {

    private static final double TOLERANCE = 1e-12;
    private static final Radius INNER = new Radius(5e-4);
    private static final Radius OUTER = new Radius(8e-4);
    // A band that lets through half of the light that crosses it perpendicularly: exp(-ln 2) = 1/2
    private static final double HALVING = Math.log(2.0);

    private final RingBand SUT = new RingBand(INNER, OUTER, HALVING);

    // A slanted ray crosses 1 / cosine times as many particles: it is halved that many times
    @ParameterizedTest
    @CsvSource(textBlock = """
            1.0,  0.5
            0.5,  0.75
            0.25, 0.9375
            0.0,  1.0
            """)
    void aBandStopsMoreOfARayTheMoreTheRaySlants(double cosine, double stopped) {
        assertThat(SUT.opacity(cosine)).isCloseTo(stopped, within(TOLERANCE));
    }
}
