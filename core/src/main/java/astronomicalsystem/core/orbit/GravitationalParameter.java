package astronomicalsystem.core.orbit;

import astronomicalsystem.core.math.Quantities;

/**
 * Gravitational parameter {@code G(M + m)} of a two-body orbit, in AU³ per day squared.
 */
public record GravitationalParameter(double auCubedPerDaySquared) {

    public GravitationalParameter {
        Quantities.requirePositiveFinite(auCubedPerDaySquared, "Gravitational parameter");
    }
}
