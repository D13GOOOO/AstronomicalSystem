package astronomicalsystem.core.impact;

import astronomicalsystem.core.math.Vector3;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApproachTest {

    @ParameterizedTest(name = "{0}")
    @CsvSource(textBlock = """
            same direction,     1.0, 0.0, 0.0,  2.0, 0.0, 0.0
            opposite direction, 0.0, 1.0, 0.0,  0.0, -3.0, 0.0
            no direction from,  0.0, 0.0, 0.0,  0.0, 0.0, 1.0
            no direction aside, 0.0, 0.0, 1.0,  0.0, 0.0, 0.0
            """)
    void directionsAlongTheSameLineAreRejected(String description, double fromX, double fromY, double fromZ,
                                               double asideX, double asideY, double asideZ) {
        Vector3 from = new Vector3(fromX, fromY, fromZ);
        Vector3 aside = new Vector3(asideX, asideY, asideZ);

        assertThatThrownBy(() -> new Approach(from, aside))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Approach directions must not be parallel");
    }
}
