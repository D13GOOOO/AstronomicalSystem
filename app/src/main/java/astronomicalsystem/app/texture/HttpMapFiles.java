package astronomicalsystem.app.texture;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.time.Duration;

/**
 * Fetches the files of the maps over HTTP with the client of the JDK.
 */
public final class HttpMapFiles implements MapFiles {

    /**
     * @param timeout the longest wait, both to connect and for the whole file
     */
    public HttpMapFiles(@NotNull Duration timeout) {
    }

    @Override
    public byte @NotNull [] fetch(@NotNull URI address) {
        return new byte[0];
    }
}
