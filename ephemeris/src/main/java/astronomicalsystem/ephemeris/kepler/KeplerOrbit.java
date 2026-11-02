package astronomicalsystem.ephemeris.kepler;

import astronomicalsystem.core.orbit.OrbitalElements;
import astronomicalsystem.ephemeris.BodyProperties;
import org.jetbrains.annotations.NotNull;

/**
 * A body and the elements of its orbit around the central body of a {@link KeplerEphemeris}.
 */
public record KeplerOrbit(@NotNull BodyProperties body, @NotNull OrbitalElements elements) {
}
