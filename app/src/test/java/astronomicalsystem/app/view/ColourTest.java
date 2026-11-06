package astronomicalsystem.app.view;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ColourTest {

    @ParameterizedTest
    @CsvSource(textBlock = """
            -0.1, 0.0, 0.0
             1.1, 0.0, 0.0
             NaN, 0.0, 0.0
             0.0, 2.0, 0.0
             0.0, NaN, 0.0
             0.0, 0.0, -1.0
             0.0, 0.0, NaN
            """)
    void constructorRejectsIntensitiesOutsideTheUnitInterval(double red, double green, double blue) {
        assertThatThrownBy(() -> new Colour(red, green, blue))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Colour components must be between 0 and 1");
    }
}
