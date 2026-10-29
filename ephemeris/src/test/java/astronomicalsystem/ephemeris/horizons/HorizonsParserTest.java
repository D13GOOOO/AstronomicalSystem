package astronomicalsystem.ephemeris.horizons;

import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.orbit.StateVector;
import astronomicalsystem.core.time.JulianDate;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HorizonsParserTest {

    // Real response for Earth at JD 2461041.5 TDB (2026-01-01), barycentric, ecliptic J2000
    private static final String EARTH_RESPONSE = "earth-2026-01-01.txt";
    private static final double EARTH_EPOCH = 2461041.5;
    private static final Vector3 EARTH_POSITION =
            new Vector3(-1.773487970879621E-01, 9.622255911830275E-01, 7.223908380425527E-05);
    private static final Vector3 EARTH_VELOCITY =
            new Vector3(-1.719737129724856E-02, -3.116429209131983E-03, 1.012925163112427E-07);
    // Real response to a query for a body that does not exist: an error text, no markers
    private static final String UNKNOWN_BODY_RESPONSE = "unknown-body.txt";

    private final HorizonsParser SUT = new HorizonsParser();

    @Test
    void readsTheStateVectorBetweenTheMarkers() {
        String response = fixture(EARTH_RESPONSE);

        StateVector state = SUT.parse(response).state();

        assertThat(state).isEqualTo(new StateVector(EARTH_POSITION, EARTH_VELOCITY));
    }

    @Test
    void readsTheEpochOfTheRow() {
        String response = fixture(EARTH_RESPONSE);

        JulianDate epoch = SUT.parse(response).epoch();

        assertThat(epoch).isEqualTo(new JulianDate(EARTH_EPOCH));
    }

    @Test
    void rejectsAResponseWithoutMarkers() {
        String response = fixture(UNKNOWN_BODY_RESPONSE);

        assertThatThrownBy(() -> SUT.parse(response))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Horizons response is missing the $$SOE/$$EOE markers");
    }

    private static String fixture(String name) {
        try (InputStream in = Objects.requireNonNull(
                HorizonsParserTest.class.getResourceAsStream("/horizons/" + name), "missing fixture " + name)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
