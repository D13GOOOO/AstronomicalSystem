package astronomicalsystem.core.orbit;

import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.math.Quantities;
import org.jetbrains.annotations.NotNull;

/**
 * Gravitational parameter {@code G(M + m)} of a two-body orbit, in AU³ per day squared.
 */
public record GravitationalParameter(double auCubedPerDaySquared) {

    /**
     * Newton's constant in AU³ per solar mass per day squared: the square of Gauss's constant
     * {@code k = 0.01720209895}.
     */
    public static final double GRAVITATIONAL_CONSTANT = 2.959122082855911e-4;

    public GravitationalParameter {
        Quantities.requirePositiveFinite(auCubedPerDaySquared, "Gravitational parameter");
    }

    /**
     * @return the parameter of the orbit of a body of mass {@code orbiting} around one of mass
     *         {@code central}
     */
    public static @NotNull GravitationalParameter of(@NotNull Mass central, @NotNull Mass orbiting) {
        return new GravitationalParameter(GRAVITATIONAL_CONSTANT * central.plus(orbiting).solarMasses());
    }
}
