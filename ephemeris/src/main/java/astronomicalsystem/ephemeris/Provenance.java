package astronomicalsystem.ephemeris;

/**
 * How the positions of an ephemeris were obtained.
 */
public enum Provenance {
    /**
     * Asked to the JPL Horizons system, which integrates the solar system with every known force.
     */
    JPL_HORIZONS,
    /**
     * Computed from orbital elements, each body on its own two-body orbit.
     */
    KEPLER_ELEMENTS
}
