package astronomicalsystem.core.body;

import astronomicalsystem.core.math.Quantities;

/**
 * Mass of a body, in solar masses.
 */
public record Mass(double solarMasses) {

    public Mass {
        Quantities.requirePositiveFinite(solarMasses, "Mass");
    }
}
