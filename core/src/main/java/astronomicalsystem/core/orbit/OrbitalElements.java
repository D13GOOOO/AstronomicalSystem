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
        double eccentricAnomaly = KeplerEquation.eccentricAnomaly(meanAnomaly, eccentricity);
        double cos = Math.cos(eccentricAnomaly);
        double sin = Math.sin(eccentricAnomaly);
        double e = eccentricity.value();
        double semiMinorAxis = semiMajorAxis * Math.sqrt(1.0 - e * e);
        double meanMotion = Math.sqrt(gravitationalParameter / (semiMajorAxis * semiMajorAxis * semiMajorAxis));
        // dE/dt, from the derivative of Kepler's equation: n = (1 - e cos E) dE/dt
        double eccentricAnomalyRate = meanMotion / (1.0 - e * cos);
        // Perifocal frame: origin in the focus, x towards the periapsis
        Vector3 position = new Vector3(semiMajorAxis * (cos - e), semiMinorAxis * sin, 0.0);
        Vector3 velocity = new Vector3(-semiMajorAxis * sin, semiMinorAxis * cos, 0.0).times(eccentricAnomalyRate);
        return new StateVector(position, velocity);
    }
}
