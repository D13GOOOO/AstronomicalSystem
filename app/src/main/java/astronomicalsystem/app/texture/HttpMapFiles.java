package astronomicalsystem.app.texture;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Fetches the files of the maps over HTTP with the client of the JDK.
 */
public final class HttpMapFiles implements MapFiles {

    private static final int HTTP_OK = 200;

    private final HttpClient client;
    private final Duration timeout;

    /**
     * @param timeout the longest wait, both to connect and for the whole file
     */
    public HttpMapFiles(@NotNull Duration timeout) {
        this.client = HttpClient.newBuilder().connectTimeout(timeout).build();
        this.timeout = timeout;
    }

    @Override
    public byte @NotNull [] fetch(@NotNull URI address) {
        HttpResponse<byte[]> response = send(HttpRequest.newBuilder(address).timeout(timeout).GET().build());
        if (response.statusCode() != HTTP_OK) {
            throw new MapUnavailableException("The map server answered with HTTP status " + response.statusCode());
        }
        return response.body();
    }

    private @NotNull HttpResponse<byte[]> send(@NotNull HttpRequest request) {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException e) {
            throw new MapUnavailableException("The map server cannot be reached", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MapUnavailableException("The download of a map was interrupted", e);
        }
    }
}
