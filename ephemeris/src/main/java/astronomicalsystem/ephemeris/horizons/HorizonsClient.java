package astronomicalsystem.ephemeris.horizons;

import astronomicalsystem.ephemeris.EphemerisEntry;
import astronomicalsystem.ephemeris.EphemerisUnavailableException;
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

    /**
     * Returns the entry that Horizons gives for the query.
     *
     * @throws EphemerisUnavailableException if Horizons cannot be reached or does not answer
     * @throws IllegalArgumentException      if the answer is not a single valid state vector
     */
    public @NotNull EphemerisEntry entryOf(@NotNull HorizonsQuery query) {
        return parser.parse(transport.fetch(query.uri()));
    }
}
