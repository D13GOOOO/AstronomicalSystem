package astronomicalsystem.ephemeris;

import astronomicalsystem.core.orbit.StateVector;
import astronomicalsystem.core.time.JulianDate;
import org.jetbrains.annotations.NotNull;

/**
 * State of a body at a given epoch.
 */
public record EphemerisEntry(@NotNull JulianDate epoch, @NotNull StateVector state) {
}
