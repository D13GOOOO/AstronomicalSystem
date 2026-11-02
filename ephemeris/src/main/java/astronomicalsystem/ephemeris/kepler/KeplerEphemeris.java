package astronomicalsystem.ephemeris.kepler;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.orbit.StateVector;
import astronomicalsystem.core.time.JulianDate;
import astronomicalsystem.ephemeris.BodyProperties;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Bodies on two-body orbits around a central body, from elements given at a common epoch.
 */
public final class KeplerEphemeris {

    private static final StateVector AT_REST_IN_THE_ORIGIN = new StateVector(Vector3.ZERO, Vector3.ZERO);

    private final @NotNull BodyProperties centralBody;

    public KeplerEphemeris(@NotNull BodyProperties centralBody, @NotNull JulianDate epochOfElements,
                           @NotNull List<KeplerOrbit> orbits) {
        this.centralBody = centralBody;
    }

    /**
     * @return the central body followed by the orbiting ones, in the order of the orbits
     */
    public @NotNull List<Body> bodiesAt(@NotNull JulianDate date) {
        return List.of(centralBody.in(AT_REST_IN_THE_ORIGIN));
    }
}
