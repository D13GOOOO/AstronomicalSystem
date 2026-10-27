package astronomicalsystem.core.orbit;

import org.jetbrains.annotations.NotNull;

/**
 * Kepler's equation {@code M = E - e sin E}, which links the mean anomaly {@code M} of an elliptic
 * orbit to its eccentric anomaly {@code E}.
 */
public final class KeplerEquation {

    private KeplerEquation() {
    }

    /**
     * @param meanAnomaly the mean anomaly, in radians, between -π and π
     * @return the eccentric anomaly, in radians
     */
    public static double eccentricAnomaly(double meanAnomaly, @NotNull Eccentricity eccentricity) {
        return meanAnomaly;
    }
}
