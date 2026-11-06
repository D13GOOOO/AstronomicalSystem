package astronomicalsystem.app.presenter;

import astronomicalsystem.app.view.Colour;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BodyStyleTest {

    private static final Colour WHITE = new Colour(1.0, 1.0, 1.0);
    private static final double HALF_A_PIXEL = 0.5;
    private static final double SUN_LIKE = 18.0;

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY})
    void constructorRejectsNonPositiveOrNonFiniteDiameters(double minimumDiameter) {
        assertThatThrownBy(() -> new BodyStyle(WHITE, minimumDiameter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Minimum diameter must be positive and finite");
    }

    @ParameterizedTest
    @ValueSource(doubles = {HALF_A_PIXEL, SUN_LIKE})
    void constructorAcceptsPositiveFiniteDiameters(double minimumDiameter) {
        assertThatNoException().isThrownBy(() -> new BodyStyle(WHITE, minimumDiameter));
    }
}
