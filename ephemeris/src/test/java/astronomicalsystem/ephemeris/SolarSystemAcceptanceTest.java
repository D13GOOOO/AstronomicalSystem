package astronomicalsystem.ephemeris;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.ephemeris.horizons.HorizonsParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static astronomicalsystem.ephemeris.horizons.HorizonsFixtures.fixture;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Acceptance test: the offline catalogue agrees with the real ephemerides of JPL Horizons.
 */
class SolarSystemAcceptanceTest {

    // Standish's mean elements are good to about 0.3% of the distance between 1800 and 2050 (Saturn)
    private static final double RELATIVE_TOLERANCE = 0.01;

    // Horizons gives the barycentre of each planetary system, as the catalogue has no moons
    @ParameterizedTest(name = "{0}")
    @CsvSource(textBlock = """
            Sun,     sun-2026-01-01.txt
            Mercury, mercury-system-2026-01-01.txt
            Venus,   venus-system-2026-01-01.txt
            Earth,   earth-moon-system-2026-01-01.txt
            Mars,    mars-system-2026-01-01.txt
            Jupiter, jupiter-system-2026-01-01.txt
            Saturn,  saturn-system-2026-01-01.txt
            Uranus,  uranus-system-2026-01-01.txt
            Neptune, neptune-system-2026-01-01.txt
            """)
    void theMeanOrbitsPutEveryBodyCloseToWhereHorizonsDoes(String name, String horizonsResponse) {
        EphemerisEntry horizons = new HorizonsParser().parse(fixture(horizonsResponse));

        Body body = named(name, SolarSystem.meanOrbits().bodiesAt(horizons.epoch()));

        assertThat(body.position().minus(horizons.state().position()).length())
                .isLessThan(RELATIVE_TOLERANCE * horizons.state().position().length());
        assertThat(body.velocity().minus(horizons.state().velocity()).length())
                .isLessThan(RELATIVE_TOLERANCE * horizons.state().velocity().length());
    }

    private static Body named(String name, List<Body> bodies) {
        return bodies.stream().filter(body -> body.name().value().equals(name)).findFirst().orElseThrow();
    }
}
