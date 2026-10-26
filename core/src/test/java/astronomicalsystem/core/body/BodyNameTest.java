package astronomicalsystem.core.body;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BodyNameTest {

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t"})
    void constructorRejectsBlankValues(String value) {
        assertThatThrownBy(() -> new BodyName(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Body name must not be blank");
    }
}
