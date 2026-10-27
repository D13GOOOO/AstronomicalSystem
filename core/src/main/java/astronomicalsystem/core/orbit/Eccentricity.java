package astronomicalsystem.core.orbit;

/**
 * Eccentricity of an elliptic orbit: 0 for a circle, approaching 1 for a very elongated ellipse.
 */
public record Eccentricity(double value) {

    public Eccentricity {
        // Written so that NaN, for which every comparison is false, is rejected too
        boolean elliptic = value >= 0.0 && value < 1.0;
        if (!elliptic) {
            throw new IllegalArgumentException("Eccentricity must be at least 0 and less than 1");
        }
    }
}
