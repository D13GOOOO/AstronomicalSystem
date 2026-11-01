package astronomicalsystem.ephemeris.horizons;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.time.Duration;

/**
 * Transport over HTTP with the client of the JDK.
 */
public final class JdkHttpTransport implements Transport {

    private final @NotNull Duration timeout;

    public JdkHttpTransport(@NotNull Duration timeout) {
        this.timeout = timeout;
    }

    @Override
    public @NotNull String fetch(@NotNull URI uri) {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
