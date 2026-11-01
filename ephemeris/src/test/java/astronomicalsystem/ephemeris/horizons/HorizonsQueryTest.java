package astronomicalsystem.ephemeris.horizons;

import astronomicalsystem.core.time.JulianDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HorizonsQueryTest {

    private static final String EARTH = "399";
    private static final JulianDate JANUARY_FIRST_2026 = new JulianDate(2461041.5);
    // The query that downloaded the fixture earth-2026-01-01.txt
    private static final URI EARTH_QUERY = URI.create("https://ssd.jpl.nasa.gov/api/horizons.api"
            + "?format=text&COMMAND=%27399%27&OBJ_DATA=%27NO%27&MAKE_EPHEM=%27YES%27&EPHEM_TYPE=%27VECTORS%27"
            + "&CENTER=%27@0%27&TLIST=%272461041.5%27&REF_PLANE=%27ECLIPTIC%27&OUT_UNITS=%27AU-D%27"
            + "&CSV_FORMAT=%27YES%27&VEC_TABLE=%272%27");
    // Shortly after the origin of Julian dates (4713 BC, still covered by DE441): Double.toString
    // writes it in scientific notation, which Horizons does not read as a date
    private static final JulianDate NEAR_THE_ORIGIN = new JulianDate(0.0001);

    @Test
    void buildsTheBarycentricEclipticVectorQueryForOneEpoch() {
        HorizonsQuery SUT = new HorizonsQuery(EARTH, JANUARY_FIRST_2026);

        URI uri = SUT.uri();

        assertThat(uri).isEqualTo(EARTH_QUERY);
    }

    @Test
    void writesTheEpochInPlainDecimalNotation() {
        HorizonsQuery SUT = new HorizonsQuery(EARTH, NEAR_THE_ORIGIN);

        String query = SUT.uri().getQuery();

        assertThat(query).contains("TLIST='0.0001'");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Earth", "399'&CENTER='@10", "+399", " 399"})
    void rejectsACommandThatIsNotANumericBodyIdentifier(String command) {
        assertThatThrownBy(() -> new HorizonsQuery(command, JANUARY_FIRST_2026))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Horizons command must be a numeric body identifier");
    }
}
