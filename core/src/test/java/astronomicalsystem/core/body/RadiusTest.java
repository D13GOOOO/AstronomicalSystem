package astronomicalsystem.core.body;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RadiusTest {

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY})
    void constructorRejectsNonPositiveOrNonFiniteValues(double astronomicalUnits) {
        assertThatThrownBy(() -> new Radius(astronomicalUnits))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Radius must be positive and finite");
    }
}
