package astronomicalsystem.ephemeris.horizons;

import astronomicalsystem.ephemeris.EphemerisEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Turns the text of a Horizons response into an ephemeris entry.
 */
public interface ResponseParser {

    /**
     * @throws IllegalArgumentException if the response does not contain exactly one valid entry
     */
    @NotNull EphemerisEntry parse(@NotNull String response);
}
