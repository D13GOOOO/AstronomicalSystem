package astronomicalsystem.core.body;

import astronomicalsystem.core.math.Quantities;

/**
 * Radius of a body, in astronomical units.
 */
public record Radius(double astronomicalUnits) {

    public Radius {
        Quantities.requirePositiveFinite(astronomicalUnits, "Radius");
    }
}
