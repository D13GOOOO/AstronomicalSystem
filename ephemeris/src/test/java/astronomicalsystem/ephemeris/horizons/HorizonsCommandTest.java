package astronomicalsystem.ephemeris.horizons;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HorizonsCommandTest {

    @ParameterizedTest
    @ValueSource(strings = {"", "Earth", "399'&CENTER='@10", "+399", " 399"})
    void rejectsAValueThatIsNotANumericBodyIdentifier(String value) {
        assertThatThrownBy(() -> new HorizonsCommand(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Horizons command must be a numeric body identifier");
    }

    // Earth, and the James Webb Space Telescope: spacecraft have negative identifiers
    @ParameterizedTest
    @ValueSource(strings = {"399", "-170"})
    void acceptsTheIdentifiersOfBodiesAndSpacecraft(String value) {
        assertThatNoException().isThrownBy(() -> new HorizonsCommand(value));
    }
}
