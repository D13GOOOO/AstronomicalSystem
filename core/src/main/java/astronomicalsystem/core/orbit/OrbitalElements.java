package astronomicalsystem.core.orbit;

import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

/**
 * Keplerian elements of an elliptic orbit, referred to the ecliptic. Angles are in radians.
 *
 * @param semiMajorAxis            in AU
 * @param inclination              angle between the orbital plane and the ecliptic
 * @param longitudeOfAscendingNode angle from the reference direction to the ascending node
 * @param argumentOfPeriapsis      angle from the ascending node to the periapsis
 * @param meanAnomaly              mean anomaly at the epoch of the elements, between -π and π
 */
public record OrbitalElements(double semiMajorAxis,
                             @NotNull Eccentricity eccentricity,
                             double inclination,
                             double longitudeOfAscendingNode,
                             double argumentOfPeriapsis,
                             double meanAnomaly) {

    /**
     * @param gravitationalParameter {@code G(M + m)}, in AU³ per day squared
     * @return the position and velocity relative to the central body at the epoch of the elements
     */
    public @NotNull StateVector stateVector(double gravitationalParameter) {
        return new StateVector(Vector3.ZERO, Vector3.ZERO);
    }
}
