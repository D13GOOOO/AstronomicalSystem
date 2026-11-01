package astronomicalsystem.ephemeris.horizons;

import astronomicalsystem.ephemeris.EphemerisUnavailableException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

/**
 * Transport over HTTP with the client of the JDK.
 */
public final class JdkHttpTransport implements Transport {

    // Horizons answers 200 even when it finds nothing: any other status is a failure of the service
    private static final int HTTP_OK = 200;

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
        HttpResponse<String> response = send(HttpRequest.newBuilder(uri).timeout(timeout).GET().build());
        if (response.statusCode() != HTTP_OK) {
            throw new EphemerisUnavailableException("Horizons answered with HTTP status " + response.statusCode());
        }
        return response.body();
    }

    private @NotNull HttpResponse<String> send(@NotNull HttpRequest request) {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (HttpTimeoutException e) {
            throw new EphemerisUnavailableException("Horizons did not answer within the timeout", e);
        } catch (IOException e) {
            throw new EphemerisUnavailableException("Horizons cannot be reached", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EphemerisUnavailableException("Horizons query was interrupted", e);
        }
    }
}
