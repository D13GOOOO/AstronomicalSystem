package astronomicalsystem.ephemeris.horizons;

import astronomicalsystem.ephemeris.EphemerisUnavailableException;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

/**
 * Retrieves the text found at a URI.
 */
public interface Transport {

    /**
     * Returns the text found at the URI.
     *
     * @throws EphemerisUnavailableException if the source cannot be reached or does not answer
     *                                       successfully
     */
    @NotNull String fetch(@NotNull URI uri);
}
