package astronomicalsystem.ephemeris.horizons;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Transport over HTTP with the client of the JDK.
 */
public final class JdkHttpTransport implements Transport {

    private final @NotNull HttpClient client;
    private final @NotNull Duration timeout;

    /**
     * @param timeout the longest wait, both to connect and for the whole response
     */
    public JdkHttpTransport(@NotNull Duration timeout) {
        this.client = HttpClient.newBuilder().connectTimeout(timeout).build();
        this.timeout = timeout;
    }

    @Override
    public @NotNull String fetch(@NotNull URI uri) {
        HttpRequest request = HttpRequest.newBuilder(uri).timeout(timeout).GET().build();
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("not yet managed", e);
        }
    }
}
