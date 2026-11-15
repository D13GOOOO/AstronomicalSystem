package astronomicalsystem.core.rings;

import astronomicalsystem.core.body.Radius;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class RingBandTest {

    private static final double TOLERANCE = 1e-12;
    private static final Radius INNER = new Radius(5e-4);
    private static final Radius OUTER = new Radius(8e-4);
    // A band that lets through half of the light that crosses it perpendicularly: exp(-ln 2) = 1/2
    private static final double HALVING = Math.log(2.0);

    private static final double ALONG_THE_BAND = 0.0;

    private final RingBand SUT = new RingBand(INNER, OUTER, HALVING);

    @Test
    void aBandWithoutWidthIsRejected() {
        assertThatThrownBy(() -> new RingBand(INNER, INNER, HALVING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The outer edge of a ring band must be beyond the inner one");
    }

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

    // Light in along one slant and out along the other: the share mu0 / (mu + mu0) of what the band
    // stops along the two paths together, 1 - 2^-(1/mu + 1/mu0)
    @ParameterizedTest
    @CsvSource(textBlock = """
            1.0, 1.0, 0.375
            0.5, 1.0, 0.583333333333333
            1.0, 0.5, 0.291666666666667
            """)
    void theLitSideIsBrighterSeenAslantAndDimmerLitAslant(double towardsViewer, double towardsLight,
                                                        double brightness) {
        assertThat(SUT.reflected(towardsViewer, towardsLight)).isCloseTo(brightness, within(TOLERANCE));
    }

    // Light in along one slant and out, on the other side, along the other: mu0 / (mu - mu0) of the
    // difference between what gets through along the two, 2^-(1/mu) - 2^-(1/mu0)
    @ParameterizedTest
    @CsvSource(textBlock = """
            1.0,  0.5, 0.25
            0.5,  1.0, 0.5
            0.25, 0.5, 0.375
            """)
    void theUnlitSideShinesWithTheLightScatteredThroughTheBand(double towardsViewer, double towardsLight,
                                                             double brightness) {
        assertThat(SUT.transmitted(towardsViewer, towardsLight)).isCloseTo(brightness, within(TOLERANCE));
    }

    // Where the two slants are the same the formula divides 0 by 0, and its limit holds:
    // tau / mu exp(-tau / mu), which is ln 2 / 2 for both of these
    @ParameterizedTest
    @ValueSource(doubles = {1.0, 0.5})
    void theUnlitSideSeenAsSlantedAsItIsLitHasTheBrightnessOfTheLimit(double cosine) {
        assertThat(SUT.transmitted(cosine, cosine)).isCloseTo(HALVING / 2.0, within(TOLERANCE));
    }

    // Also seen along the band, where the two cosines are both 0
    @ParameterizedTest
    @ValueSource(doubles = {1.0, 0.5, 0.0})
    void aBandLitAlongItselfSendsBackNoLight(double towardsViewer) {
        assertThat(SUT.reflected(towardsViewer, ALONG_THE_BAND)).isZero();
    }

    @ParameterizedTest
    @ValueSource(doubles = {1.0, 0.5, 0.0})
    void aBandLitAlongItselfLetsNoLightThrough(double towardsViewer) {
        assertThat(SUT.transmitted(towardsViewer, ALONG_THE_BAND)).isZero();
    }
}
