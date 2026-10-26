package astronomicalsystem.core.body;

/**
 * Radius of a body, in astronomical units.
 */
public record Radius(double astronomicalUnits) {

    public Radius {
        if (astronomicalUnits <= 0.0 || !Double.isFinite(astronomicalUnits)) {
            throw new IllegalArgumentException("Radius must be positive and finite");
        }
    }
}
