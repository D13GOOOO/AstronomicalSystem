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
        List<Body> aroundTheCentralBody = Stream.concat(Stream.of(centralBody.in(AT_REST_IN_THE_ORIGIN)),
                orbits.stream().map(this::onItsOrbit)).toList();
        StateVector centreOfMass = centreOfMass(aroundTheCentralBody);
        return aroundTheCentralBody.stream().map(body -> relativeTo(centreOfMass, body)).toList();
    }

    private static @NotNull StateVector centreOfMass(@NotNull List<Body> bodies) {
        double totalMass = 0.0;
        Vector3 massMoment = Vector3.ZERO;
        Vector3 momentum = Vector3.ZERO;
        for (Body body : bodies) {
            double mass = body.mass().solarMasses();
            totalMass += mass;
            massMoment = massMoment.plus(body.position().times(mass));
            momentum = momentum.plus(body.velocity().times(mass));
        }
        return new StateVector(massMoment.times(1.0 / totalMass), momentum.times(1.0 / totalMass));
    }

    private static @NotNull Body relativeTo(@NotNull StateVector origin, @NotNull Body body) {
        return body.withMotion(body.position().minus(origin.position()), body.velocity().minus(origin.velocity()));
    }

    private @NotNull Body onItsOrbit(@NotNull KeplerOrbit orbit) {
        BodyProperties body = orbit.body();
        GravitationalParameter gravitationalParameter = GravitationalParameter.of(centralBody.mass(), body.mass());
        return body.in(orbit.elements().stateVector(gravitationalParameter));
    }
}
