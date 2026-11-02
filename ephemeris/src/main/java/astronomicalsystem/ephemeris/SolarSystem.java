package astronomicalsystem.ephemeris;

import astronomicalsystem.ephemeris.kepler.KeplerEphemeris;
import org.jetbrains.annotations.NotNull;

/**
 * Catalogue of the Sun and the planets.
 */
public final class SolarSystem {

    private SolarSystem() {
    }

    /**
     * @return the Sun and the planets on their mean orbits around it
     */
    public static @NotNull KeplerEphemeris meanOrbits() {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
