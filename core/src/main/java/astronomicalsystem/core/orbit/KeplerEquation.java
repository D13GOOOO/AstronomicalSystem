package astronomicalsystem.core.orbit;

import org.jetbrains.annotations.NotNull;

/**
 * Kepler's equation {@code M = E - e sin E}, which links the mean anomaly {@code M} of an elliptic
 * orbit to its eccentric anomaly {@code E}.
 */
public final class KeplerEquation {

    // Starting guess of Danby (1987), E = M + 0.85 e sgn(sin M): Newton's method converges from it
    // for every elliptic eccentricity, even close to 1
    private static final double DANBY_FACTOR = 0.85;
    // Newton converges quadratically, so the error left after a correction this small is negligible
    private static final double TOLERANCE = 1e-14;

    private KeplerEquation() {
    }

    /**
     * @param meanAnomaly the mean anomaly, in radians, between -π and π
     * @return the eccentric anomaly, in radians
     */
    public static double eccentricAnomaly(double meanAnomaly, @NotNull Eccentricity eccentricity) {
        double e = eccentricity.value();
        double eccentricAnomaly = meanAnomaly + DANBY_FACTOR * e * Math.signum(Math.sin(meanAnomaly));
        double correction;
        do {
            correction = (eccentricAnomaly - e * Math.sin(eccentricAnomaly) - meanAnomaly)
                    / (1.0 - e * Math.cos(eccentricAnomaly));
            eccentricAnomaly -= correction;
        } while (Math.abs(correction) >= TOLERANCE);
        return eccentricAnomaly;
    }
}
