package astronomicalsystem.core.simulation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SamplingTest {

    @ParameterizedTest
    @CsvSource(textBlock = """
             0,  2,  Steps per point must be positive
            -1,  2,  Steps per point must be positive
             1,  1,  A trajectory needs at least two points
             1,  0,  A trajectory needs at least two points
            """)
    void constructorRejectsSamplingsWithoutALine(int stepsPerPoint, int points, String message) {
        assertThatThrownBy(() -> new Sampling(stepsPerPoint, points))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(message);
    }
}
