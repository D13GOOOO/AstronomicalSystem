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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpMapFilesTest {

    private static final String PATH = "/tiles/0/0/0.jpg";
    private static final int OK = 200;
    private static final int NOT_FOUND = 404;
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

    // The map servers answer 404 with a page of text for a tile that does not exist
    @Test
    void anAnswerThatIsNotASuccessIsNotAFile() {
        respondWith(NOT_FOUND, TILE);

        assertThatThrownBy(() -> SUT.fetch(address()))
                .isInstanceOf(MapUnavailableException.class)
                .hasMessage("The map server answered with HTTP status 404");
    }

    @Test
    void aServerThatIsNotThereIsReportedAsUnavailable() {
        URI address = address();
        server.stop(0);

        assertThatThrownBy(() -> SUT.fetch(address))
                .isInstanceOf(MapUnavailableException.class)
                .hasMessage("The map server cannot be reached");
    }

    // The thread is asked to stop while it downloads: it says so and stays interrupted, for whoever
    // runs it to see
    @Test
    void anInterruptedDownloadLeavesTheThreadInterrupted() {
        respondWith(OK, TILE);
        Thread.currentThread().interrupt();

        assertThatThrownBy(() -> SUT.fetch(address()))
                .isInstanceOf(MapUnavailableException.class)
                .hasMessage("The download of a map was interrupted");
        assertThat(Thread.interrupted()).isTrue();
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
