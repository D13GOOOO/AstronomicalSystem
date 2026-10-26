package astronomicalsystem.core.math;

import org.jetbrains.annotations.NotNull;

/**
 * Validation of scalar physical quantities.
 */
public final class Quantities {

    private Quantities() {
    }

    public static void requirePositiveFinite(double value, @NotNull String quantityName) {
        if (value <= 0.0 || !Double.isFinite(value)) {
            throw new IllegalArgumentException(quantityName + " must be positive and finite");
        }
    }
}
