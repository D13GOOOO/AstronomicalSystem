package astronomicalsystem.ephemeris.kepler;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.orbit.GravitationalParameter;
import astronomicalsystem.core.orbit.OrbitalElements;
import astronomicalsystem.ephemeris.BodyProperties;
import org.jetbrains.annotations.NotNull;

/**
 * A body and the elements of its orbit around the central body of a {@link KeplerEphemeris}.
 */
public record KeplerOrbit(@NotNull BodyProperties body, @NotNull OrbitalElements elements) {

    /**
     * @param daysSinceEpoch a finite time since the epoch of the elements
     * @return the body in its state relative to a central body of mass {@code central}
     */
    @NotNull Body around(@NotNull Mass central, double daysSinceEpoch) {
        GravitationalParameter gravitationalParameter = GravitationalParameter.of(central, body.mass());
        return body.in(elements.propagated(daysSinceEpoch, gravitationalParameter)
                .stateVector(gravitationalParameter));
    }
}
