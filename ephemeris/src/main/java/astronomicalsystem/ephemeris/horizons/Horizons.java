package astronomicalsystem.ephemeris.horizons;

import astronomicalsystem.ephemeris.EphemerisEntry;
import astronomicalsystem.ephemeris.EphemerisUnavailableException;
import org.jetbrains.annotations.NotNull;

/**
 * NASA JPL Horizons, as seen by the sources of ephemerides that ask it for state vectors.
 */
public interface Horizons {

    /**
     * Returns the entry that Horizons gives for the query.
     *
     * @throws EphemerisUnavailableException if Horizons cannot be reached or does not answer
     * @throws IllegalArgumentException      if the answer is not a single valid state vector
     */
    @NotNull EphemerisEntry entryOf(@NotNull HorizonsQuery query);
}
