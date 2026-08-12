package astronomicalsystem.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InputHandlerTest {

    @Test
    void clampKeepsValuesWithinBounds() {
        assertEquals(-500.0, InputHandler.clamp(-500.0, -100000.0, -200.0), 1e-9);
        assertEquals(-3000.0, InputHandler.clamp(-3000.0, -100000.0, -200.0), 1e-9);
        assertEquals(-100000.0, InputHandler.clamp(-150000.0, -100000.0, -200.0), 1e-9);
    }
}
