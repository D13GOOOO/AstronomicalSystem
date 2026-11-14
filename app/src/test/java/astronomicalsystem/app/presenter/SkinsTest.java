package astronomicalsystem.app.presenter;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.rotation.Spin;
import astronomicalsystem.core.time.JulianDate;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SkinsTest {

    private static final Spin STILL = new Spin(new Vector3(0.0, 0.0, 1.0), new Vector3(1.0, 0.0, 0.0),
            new JulianDate(2451545.0), 0.0);
    private static final Skin OF_MARS = new Skin("mars", STILL);
    private static final Skin OF_A_PLANET = new Skin("moon", STILL);
    private static final Skins SUT = new Skins(Map.of(new BodyName("Mars"), OF_MARS),
            Map.of(BodyKind.PLANET, OF_A_PLANET));

    @Test
    void aBodyKnownByNameHasItsOwnSkin() {
        Optional<Skin> skin = SUT.of(body("Mars", BodyKind.PLANET));

        assertThat(skin).contains(OF_MARS);
    }

    private static Body body(String name, BodyKind kind) {
        return new Body(new BodyName(name), kind, new Mass(1e-7), new Radius(1e-5), Vector3.ZERO, Vector3.ZERO);
    }
}
