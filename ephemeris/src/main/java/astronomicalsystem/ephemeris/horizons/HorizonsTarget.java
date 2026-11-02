package astronomicalsystem.ephemeris.horizons;

import astronomicalsystem.ephemeris.BodyProperties;
import org.jetbrains.annotations.NotNull;

/**
 * A body of a catalogue and the identifier under which Horizons knows it.
 */
public record HorizonsTarget(@NotNull BodyProperties body, @NotNull HorizonsCommand command) {
}
