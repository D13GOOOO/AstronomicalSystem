package astronomicalsystem.ephemeris;

import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.rings.RingBand;
import astronomicalsystem.core.rings.Rings;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SolarSystemRingsTest {

    private static final BodyName SATURN = new BodyName("Saturn");
    private static final double KILOMETRES_PER_AU = 149_597_870.7;

    // The edges of the C ring, the B ring, the Cassini Division and the A ring, in kilometres from
    // the centre of Saturn, and how much light each stops
    @Test
    void onlySaturnHasBrightRingsAndTheyLieAcrossItsPole() {
        Map<BodyName, Rings> rings = SolarSystem.rings();

        assertThat(rings).containsOnlyKeys(SATURN);
        assertThat(rings.get(SATURN).pole()).isEqualTo(SolarSystem.spins().get(SATURN).pole());
        assertThat(rings.get(SATURN).bands()).containsExactly(
                band(74_658.0, 92_000.0, 0.1),
                band(92_000.0, 117_580.0, 1.5),
                band(117_580.0, 122_170.0, 0.05),
                band(122_170.0, 136_775.0, 0.5));
    }

    private static RingBand band(double innerKilometres, double outerKilometres, double opticalDepth) {
        return new RingBand(new Radius(innerKilometres / KILOMETRES_PER_AU),
                new Radius(outerKilometres / KILOMETRES_PER_AU), opticalDepth);
    }
}
