package astronomicalsystem.app.camera;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThatNoException;
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

    // A pinhole, a fish-eye just short of a half turn, and the lens of the application
    @ParameterizedTest
    @CsvSource(textBlock = """
            1e-6,               1e-9, 2e-9
            3.14159,            0.01, 1000.0
            0.7853981633974483, 0.01, 1000.0
            """)
    void constructorAcceptsPossibleOptics(double verticalFieldOfView, double near, double far) {
        assertThatNoException().isThrownBy(() -> new Lens(verticalFieldOfView, near, far));
    }
}
