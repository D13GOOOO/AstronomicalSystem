package astronomicalsystem.core.body;

/**
 * Mass of a body, in solar masses.
 */
public record Mass(double solarMasses) {

    public Mass {
        if (solarMasses <= 0.0 || !Double.isFinite(solarMasses)) {
            throw new IllegalArgumentException("Mass must be positive and finite");
        }
    }
}
