package astronomicalsystem.ephemeris.horizons;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class JdkHttpTransportTest {

    private static final String PATH = "/horizons";
    private static final int OK = 200;
    private static final String BODY = "$$SOE\nrow\n$$EOE\n";
    private static final Duration TIMEOUT = Duration.ofSeconds(2);
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

    private void respondWith(int status, String body) {
        server.createContext(PATH, exchange -> {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        });
    }

    private URI serverUri() {
        InetSocketAddress address = server.getAddress();
        return URI.create("http://" + address.getHostString() + ":" + address.getPort() + PATH);
    }
}
