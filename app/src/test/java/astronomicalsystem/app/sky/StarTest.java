package astronomicalsystem.app.sky;

import astronomicalsystem.core.math.Vector3;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StarTest {

    private static final Vector3 TWO_UNITS_TOWARDS_THE_POLE = new Vector3(0.0, 0.0, 2.0);
    private static final Vector3 TOWARDS_THE_POLE = new Vector3(0.0, 0.0, 1.0);
    private static final double FAINTEST = 1.0;

    @Test
    void theDirectionOfAStarIsAUnitVector() {
        Star SUT = new Star(TWO_UNITS_TOWARDS_THE_POLE, FAINTEST);

        assertThat(SUT.direction()).isEqualTo(TOWARDS_THE_POLE);
    }

    @Test
    void aStarWithoutADirectionIsRejected() {
        assertThatThrownBy(() -> new Star(Vector3.ZERO, FAINTEST))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A star needs a direction");
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY})
    void aStarThatGivesNoLightOrAllOfItIsRejected(double flux) {
        assertThatThrownBy(() -> new Star(TOWARDS_THE_POLE, flux))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Flux of a star must be positive and finite");
    }
}
