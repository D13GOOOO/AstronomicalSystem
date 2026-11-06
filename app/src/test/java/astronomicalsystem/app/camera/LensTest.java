package astronomicalsystem.app.camera;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LensTest {

    @ParameterizedTest
    @CsvSource(textBlock = """
            0.0,               1.0, 10.0,     Field of view must be between 0 and pi radians
            3.141592653589793, 1.0, 10.0,     Field of view must be between 0 and pi radians
            NaN,               1.0, 10.0,     Field of view must be between 0 and pi radians
            1.0,               0.0, 10.0,     Near plane must be positive and finite
            1.0,               NaN, 10.0,     Near plane must be positive and finite
            1.0,               1.0, 1.0,      Far plane must be finite and beyond the near plane
            1.0,               1.0, Infinity, Far plane must be finite and beyond the near plane
            """)
    void constructorRejectsImpossibleOptics(double verticalFieldOfView, double near, double far, String message) {
        assertThatThrownBy(() -> new Lens(verticalFieldOfView, near, far))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
    }
}
