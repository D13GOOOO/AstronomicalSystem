package astronomicalsystem.ephemeris;

import astronomicalsystem.core.time.JulianDate;
import org.jetbrains.annotations.NotNull;

/**
 * Asks a primary source and turns to a fallback when the primary has no usable answer: when it is
 * unavailable or answers with something that is not an ephemeris.
 */
public final class FallbackEphemeris implements EphemerisSource {

    private final @NotNull EphemerisSource primary;
    private final @NotNull EphemerisSource fallback;

    public FallbackEphemeris(@NotNull EphemerisSource primary, @NotNull EphemerisSource fallback) {
        this.primary = primary;
        this.fallback = fallback;
    }

    @Override
    public @NotNull Ephemeris ephemerisAt(@NotNull JulianDate date) {
        try {
            return primary.ephemerisAt(date);
        } catch (EphemerisUnavailableException | IllegalArgumentException e) {
            return fallback.ephemerisAt(date);
        }
    }
}
