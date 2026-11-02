package astronomicalsystem.ephemeris.horizons;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.time.JulianDate;
import astronomicalsystem.ephemeris.EphemerisSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The bodies of a catalogue where Horizons puts them.
 */
public final class HorizonsEphemeris implements EphemerisSource {

    public HorizonsEphemeris(@NotNull Horizons horizons, @NotNull List<HorizonsTarget> targets) {
    }

    /**
     * @return the bodies in the order of the targets
     */
    @Override
    public @NotNull List<Body> bodiesAt(@NotNull JulianDate date) {
        return List.of();
    }
}
