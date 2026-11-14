package astronomicalsystem.app.impact;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.physics.Collision;
import astronomicalsystem.core.time.JulianDate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;

class BlastTest {

    private static final double G = 2.959122082855911e-4;
    private static final double EARTH_MASS = 3.003e-6;
    private static final double EARTH_RADIUS = 4.259e-5;
    private static final Body EARTH = new Body(new BodyName("Earth"), BodyKind.PLANET, new Mass(EARTH_MASS),
            new Radius(EARTH_RADIUS), Vector3.ZERO, Vector3.ZERO);
    // The energy that would scatter the Earth: (3/5) G M² / R
    private static final double BINDING_ENERGY = 0.6 * G * EARTH_MASS * EARTH_MASS / EARTH_RADIUS;
    private static final Vector3 UP = new Vector3(0.0, 0.0, 1.0);
    // About 17 km/s
    private static final double SPEED = 0.01;
    private static final JulianDate START = new JulianDate(2461041.5);
    private static final double PRECISION_PERCENT = 1e-9;

    // An eighth of the binding energy: the fireball grows to half a radius of the Earth, cbrt(1/8),
    // at the speed of the impact, so that it takes R / 2 / v to reach it
    @Test
    void theFireballGrowsToTheCubeRootOfTheEnergyOverTheBindingEnergy() {
        Blast SUT = new Blast(new Collision(EARTH, UP, UP, SPEED, BINDING_ENERGY / 8.0), START);
        double halfARadius = EARTH_RADIUS / 2.0;

        Flare fireball = SUT.flaresAt(new JulianDate(START.days() + halfARadius / SPEED), Vector3.ZERO).getFirst();

        assertThat(fireball.radius()).isCloseTo(halfARadius, withinPercentage(PRECISION_PERCENT));
    }
}
