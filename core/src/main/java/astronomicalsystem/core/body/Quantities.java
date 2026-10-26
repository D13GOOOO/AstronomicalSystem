package astronomicalsystem.core.body;

import org.jetbrains.annotations.NotNull;

final class Quantities {

    private Quantities() {
    }

    static void requirePositiveFinite(double value, @NotNull String quantityName) {
        if (value <= 0.0 || !Double.isFinite(value)) {
            throw new IllegalArgumentException(quantityName + " must be positive and finite");
        }
    }
}
