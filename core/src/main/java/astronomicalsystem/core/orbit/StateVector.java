package astronomicalsystem.core.orbit;

import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

/**
 * Position, in AU, and velocity, in AU per day, of a body relative to a reference origin: the central
 * body for {@link OrbitalElements}, the solar-system barycentre for the ephemerides.
 */
public record StateVector(@NotNull Vector3 position, @NotNull Vector3 velocity) {
}
