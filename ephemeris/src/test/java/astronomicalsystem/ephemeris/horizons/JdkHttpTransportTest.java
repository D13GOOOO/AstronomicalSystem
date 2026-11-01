package astronomicalsystem.ephemeris.horizons;

import astronomicalsystem.ephemeris.EphemerisUnavailableException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JdkHttpTransportTest {

    private static final String PATH = "/horizons";
    private static final int OK = 200;
    private static final int SERVICE_UNAVAILABLE = 503;
    private static final String BODY = "$$SOE\nrow\n$$EOE\n";
    private static final Duration TIMEOUT = Duration.ofMillis(500);
    private static final Duration LONGER_THAN_THE_TIMEOUT = Duration.ofMillis(1500);
    // Port 0: the system picks a free one
    private static final int ANY_FREE_PORT = 0;
    private static final int DEFAULT_BACKLOG = 0;

    // A local server stands in for Horizons: the transport is checked against real HTTP, never the network
    private HttpServer server;
    private final JdkHttpTransport SUT = new JdkHttpTransport(TIMEOUT);

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), ANY_FREE_PORT),
                DEFAULT_BACKLOG);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void returnsTheBodyOfASuccessfulResponse() {
        respondWith(OK, BODY);

        String body = SUT.fetch(serverUri());

        assertThat(body).isEqualTo(BODY);
    }

    @Test
    void rejectsAnUnsuccessfulResponse() {
        respondWith(SERVICE_UNAVAILABLE, BODY);

        assertThatThrownBy(() -> SUT.fetch(serverUri()))
                .isInstanceOf(EphemerisUnavailableException.class)
                .hasMessage("Horizons answered with HTTP status 503");
    }

    @Test
    void givesUpOnAServerThatDoesNotAnswerInTime() {
        respondLate(OK, BODY);

        assertThatThrownBy(() -> SUT.fetch(serverUri()))
                .isInstanceOf(EphemerisUnavailableException.class)
                .hasMessage("Horizons did not answer within the timeout")
                .hasCauseInstanceOf(HttpTimeoutException.class);
    }

    @Test
    void reportsAServerThatCannotBeReached() {
        URI closedPort = serverUri();
        server.stop(0);

        assertThatThrownBy(() -> SUT.fetch(closedPort))
                .isInstanceOf(EphemerisUnavailableException.class)
                .hasMessage("Horizons cannot be reached")
                .hasCauseInstanceOf(ConnectException.class);
    }
    @Test
    void reportsAnInterruptedQueryAndKeepsTheInterruptStatus() {
        respondWith(OK, BODY);
        Thread.currentThread().interrupt();

        assertThatThrownBy(() -> SUT.fetch(serverUri()))
                .isInstanceOf(EphemerisUnavailableException.class)
                .hasMessage("Horizons query was interrupted")
                .hasCauseInstanceOf(InterruptedException.class);
        // Thread.interrupted() also clears the status, so the next tests run on a clean thread
        assertThat(Thread.interrupted()).isTrue();
    }
    private void respondWith(int status, String body) {
        server.createContext(PATH, exchange -> reply(exchange, status, body));
    }

    private void respondLate(int status, String body) {
        server.createContext(PATH, exchange -> {
            pause(LONGER_THAN_THE_TIMEOUT);
            reply(exchange, status, body);
        });
    }

    private static void reply(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private static void pause(Duration delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private URI serverUri() {
        InetSocketAddress address = server.getAddress();
        return URI.create("http://" + address.getHostString() + ":" + address.getPort() + PATH);
    }
}
