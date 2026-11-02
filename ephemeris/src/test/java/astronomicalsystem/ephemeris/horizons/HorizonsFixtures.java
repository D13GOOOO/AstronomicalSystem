package astronomicalsystem.ephemeris.horizons;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Real Horizons responses saved under {@code src/test/resources/horizons}.
 */
public final class HorizonsFixtures {

    private HorizonsFixtures() {
    }

    public static String fixture(String name) {
        try (InputStream in = Objects.requireNonNull(
                HorizonsFixtures.class.getResourceAsStream("/horizons/" + name), "missing fixture " + name)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
