package astronomicalsystem.app.camera;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ViewportTest {

    @ParameterizedTest
    @CsvSource(textBlock = """
             0, 100, Viewport width must be positive
            -1, 100, Viewport width must be positive
            100,  0, Viewport height must be positive
            100, -1, Viewport height must be positive
            """)
    void constructorRejectsEmptySizes(int width, int height, String message) {
        assertThatThrownBy(() -> new Viewport(width, height))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
    }
}
