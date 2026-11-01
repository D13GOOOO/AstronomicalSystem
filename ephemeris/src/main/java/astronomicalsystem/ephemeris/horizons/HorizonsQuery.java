package astronomicalsystem.ephemeris.horizons;

import astronomicalsystem.core.time.JulianDate;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

/**
 * Request to NASA JPL Horizons for the barycentric state vector of one body at one epoch.
 *
 * @param command the Horizons identifier of the body (e.g. {@code 399} for Earth)
 * @param epoch   the instant of the state vector
 */
public record HorizonsQuery(@NotNull String command, @NotNull JulianDate epoch) {

    public @NotNull URI uri() {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
