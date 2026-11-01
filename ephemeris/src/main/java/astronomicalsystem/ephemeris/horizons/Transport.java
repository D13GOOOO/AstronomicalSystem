package astronomicalsystem.ephemeris.horizons;

import org.jetbrains.annotations.NotNull;

import java.net.URI;

/**
 * Retrieves the text found at a URI.
 */
public interface Transport {

    @NotNull String fetch(@NotNull URI uri);
}
