package astronomicalsystem.view;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SaturnRingTest {

    @Test
    void validateParametersRejectsTooFewDivisions() {
        assertThrows(IllegalArgumentException.class, () -> SaturnRing.validateParameters(1.0, 2.0, 2));
    }

    @Test
    void validateParametersRejectsInvalidRadii() {
        assertThrows(IllegalArgumentException.class, () -> SaturnRing.validateParameters(0.0, 2.0, 8));
        assertThrows(IllegalArgumentException.class, () -> SaturnRing.validateParameters(5.0, 5.0, 8));
        assertThrows(IllegalArgumentException.class, () -> SaturnRing.validateParameters(6.0, 5.0, 8));
    }
}
