package astronomicalsystem.app.texture;

import org.jetbrains.annotations.NotNull;

import java.net.URI;

/**
 * Asks again for a file that could not be had: servers fail now and then, and a map is worth a
 * second try.
 */
public final class RetryingMapFiles implements MapFiles {

    private final MapFiles origin;
    private final int attempts;

    /**
     * @param attempts how many times a file is asked for before giving up; precondition: positive
     */
    public RetryingMapFiles(@NotNull MapFiles origin, int attempts) {
        this.origin = origin;
        this.attempts = attempts;
    }

    @Override
    public byte @NotNull [] fetch(@NotNull URI address) {
        for (int attempt = 1; attempt < attempts; attempt++) {
            try {
                return origin.fetch(address);
            } catch (MapUnavailableException failed) {
                // Nothing to do: the file is asked for again
            }
        }
        // The last attempt: if it fails too, whoever asked is told
        return origin.fetch(address);
    }
}
