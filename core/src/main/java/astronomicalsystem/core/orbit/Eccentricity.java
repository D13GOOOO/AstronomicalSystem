package astronomicalsystem.core.orbit;

/**
 * Eccentricity of an elliptic orbit: 0 for a circle, approaching 1 for a very elongated ellipse.
 */
public record Eccentricity(double value) {

    public Eccentricity {
    }
}
