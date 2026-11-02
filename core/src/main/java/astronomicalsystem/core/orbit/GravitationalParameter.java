package astronomicalsystem.core.orbit;

import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.math.Quantities;
import org.jetbrains.annotations.NotNull;

/**
 * Gravitational parameter {@code G(M + m)} of a two-body orbit, in AU³ per day squared.
 */
public record GravitationalParameter(double auCubedPerDaySquared) {

    public GravitationalParameter {
        Quantities.requirePositiveFinite(auCubedPerDaySquared, "Gravitational parameter");
    }

    /**
     * @return the parameter of the orbit of a body of mass {@code orbiting} around one of mass
     *         {@code central}
     */
    public static @NotNull GravitationalParameter of(@NotNull Mass central, @NotNull Mass orbiting) {
        return new GravitationalParameter(1.0);
    }
}
