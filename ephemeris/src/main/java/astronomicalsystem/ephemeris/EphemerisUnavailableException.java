package astronomicalsystem.ephemeris;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown when a source of ephemerides cannot be reached or does not answer.
 */
public class EphemerisUnavailableException extends RuntimeException {

    public EphemerisUnavailableException(@NotNull String message) {
        super(message);
    }
}
