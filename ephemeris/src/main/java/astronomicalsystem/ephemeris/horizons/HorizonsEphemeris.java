package astronomicalsystem.ephemeris.horizons;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.time.JulianDate;
import astronomicalsystem.ephemeris.Ephemeris;
import astronomicalsystem.ephemeris.EphemerisSource;
import astronomicalsystem.ephemeris.Provenance;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The bodies of a catalogue where Horizons puts them.
 */
public final class HorizonsEphemeris implements EphemerisSource {

    private final @NotNull Horizons horizons;
    private final @NotNull List<HorizonsTarget> targets;

    public HorizonsEphemeris(@NotNull Horizons horizons, @NotNull List<HorizonsTarget> targets) {
        this.horizons = horizons;
        this.targets = List.copyOf(targets);
    }

    /**
     * @return the bodies in the order of the targets
     */
    @Override
    public @NotNull List<Body> bodiesAt(@NotNull JulianDate date) {
        return targets.stream().map(target -> atDate(target, date)).toList();
    }

    /**
     * @return the bodies of {@link #bodiesAt(JulianDate)}, as Horizons gives them
     */
    @Override
    public @NotNull Ephemeris ephemerisAt(@NotNull JulianDate date) {
        return new Ephemeris(bodiesAt(date), Provenance.JPL_HORIZONS);
    }

    private @NotNull Body atDate(@NotNull HorizonsTarget target, @NotNull JulianDate date) {
        return target.body().in(horizons.entryOf(new HorizonsQuery(target.command(), date)).state());
    }
}
