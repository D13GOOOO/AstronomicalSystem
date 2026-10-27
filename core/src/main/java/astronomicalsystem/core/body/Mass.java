package astronomicalsystem.core.body;

import astronomicalsystem.core.math.Quantities;
import org.jetbrains.annotations.NotNull;

/**
 * Mass of a body, in solar masses.
 */
public record Mass(double solarMasses) {

    public Mass {
        Quantities.requirePositiveFinite(solarMasses, "Mass");
    }

    public @NotNull Mass plus(@NotNull Mass other) {
        return new Mass(solarMasses + other.solarMasses);
    }
}
