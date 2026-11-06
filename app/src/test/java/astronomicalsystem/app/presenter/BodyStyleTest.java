package astronomicalsystem.app.presenter;

import astronomicalsystem.app.view.Colour;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BodyStyleTest {

    private static final Colour WHITE = new Colour(1.0, 1.0, 1.0);

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY})
    void constructorRejectsNonPositiveOrNonFiniteDiameters(double minimumDiameter) {
        assertThatThrownBy(() -> new BodyStyle(WHITE, minimumDiameter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Minimum diameter must be positive and finite");
    }
}
