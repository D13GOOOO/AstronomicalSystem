package astronomicalsystem.ephemeris.horizons;

import astronomicalsystem.ephemeris.EphemerisEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Asks NASA JPL Horizons for the state vector of a body at an epoch.
 */
public final class HorizonsClient {

    private final @NotNull Transport transport;
    private final @NotNull ResponseParser parser;

    public HorizonsClient(@NotNull Transport transport, @NotNull ResponseParser parser) {
        this.transport = transport;
        this.parser = parser;
    }

    public @NotNull EphemerisEntry entryOf(@NotNull HorizonsQuery query) {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
