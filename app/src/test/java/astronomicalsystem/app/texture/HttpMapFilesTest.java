package astronomicalsystem.app.texture;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class HttpMapFilesTest {

    private static final String PATH = "/tiles/0/0/0.jpg";
    private static final int OK = 200;
    // Bytes that are not text: a file must arrive as it is
    private static final byte[] TILE = {(byte) 0xFF, (byte) 0xD8, 0, (byte) 0x80, 127};
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    // Port 0: the system picks a free one
    private static final int ANY_FREE_PORT = 0;
    private static final int DEFAULT_BACKLOG = 0;

    // A local server stands in for the map server: real HTTP, never the network
    private HttpServer server;
    private final HttpMapFiles SUT = new HttpMapFiles(TIMEOUT);

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
    void aFileArrivesByteForByte() {
        respondWith(OK, TILE);

        byte[] file = SUT.fetch(address());

        assertThat(file).isEqualTo(TILE);
    }

    private void respondWith(int status, byte[] body) {
        server.createContext(PATH, exchange -> {
            exchange.sendResponseHeaders(status, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
    }

    private URI address() {
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + PATH);
    }
}
