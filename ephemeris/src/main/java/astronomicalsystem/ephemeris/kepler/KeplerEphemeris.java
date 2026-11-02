package astronomicalsystem.ephemeris.kepler;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.orbit.GravitationalParameter;
import astronomicalsystem.core.orbit.StateVector;
import astronomicalsystem.core.time.JulianDate;
import astronomicalsystem.ephemeris.BodyProperties;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

/**
 * Bodies on two-body orbits around a central body, from elements given at a common epoch.
 */
public final class KeplerEphemeris {

    private static final StateVector AT_REST_IN_THE_ORIGIN = new StateVector(Vector3.ZERO, Vector3.ZERO);

    private final @NotNull BodyProperties centralBody;
    private final @NotNull List<KeplerOrbit> orbits;

    public KeplerEphemeris(@NotNull BodyProperties centralBody, @NotNull JulianDate epochOfElements,
                           @NotNull List<KeplerOrbit> orbits) {
        this.centralBody = centralBody;
        this.orbits = List.copyOf(orbits);
    }

    /**
     * @return the central body followed by the orbiting ones, in the order of the orbits
     */
    public @NotNull List<Body> bodiesAt(@NotNull JulianDate date) {
        return Stream.concat(Stream.of(centralBody.in(AT_REST_IN_THE_ORIGIN)), orbits.stream().map(this::onItsOrbit))
                .toList();
    }

    private @NotNull Body onItsOrbit(@NotNull KeplerOrbit orbit) {
        BodyProperties body = orbit.body();
        GravitationalParameter gravitationalParameter = GravitationalParameter.of(centralBody.mass(), body.mass());
        return body.in(orbit.elements().stateVector(gravitationalParameter));
    }
}
