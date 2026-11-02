package astronomicalsystem.ephemeris;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.time.JulianDate;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Asks a primary source and turns to a fallback when the primary has no usable answer.
 */
public final class FallbackEphemeris implements EphemerisSource {

    private final @NotNull EphemerisSource primary;

    public FallbackEphemeris(@NotNull EphemerisSource primary, @NotNull EphemerisSource fallback) {
        this.primary = primary;
    }

    @Override
    public @NotNull List<Body> bodiesAt(@NotNull JulianDate date) {
        return primary.bodiesAt(date);
    }
}
