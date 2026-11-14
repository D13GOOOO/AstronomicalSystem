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

import java.util.List;

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
    private static final double NINE_MINUTES = 9.0 / 1440.0;
    private static final double TEN_MINUTES = 10.0 / 1440.0;
    private static final int EMBERS = 240;
    // The age is known to some 40 microseconds out of minutes
    private static final double HEAT_PRECISION_PERCENT = 1e-4;

    // An eighth of the binding energy: the fireball grows to half a radius of the Earth, cbrt(1/8),
    // at the speed of the impact, so that it takes R / 2 / v to reach it; looked at twice as late,
    // since a Julian date of this century is precise only to some 40 microseconds
    @Test
    void theFireballGrowsToTheCubeRootOfTheEnergyOverTheBindingEnergy() {
        Blast SUT = new Blast(new Collision(EARTH, UP, UP, SPEED, BINDING_ENERGY / 8.0), START);
        double halfARadius = EARTH_RADIUS / 2.0;

        Flare fireball = SUT.flaresAt(new JulianDate(START.days() + 2.0 * halfARadius / SPEED), Vector3.ZERO)
                .getFirst();

        assertThat(fireball.radius()).isCloseTo(halfARadius, withinPercentage(PRECISION_PERCENT));
    }

    // Grown in a rise time t, it glows for twenty of them and cools as exp(-(age - t) / tau), with
    // tau a third of the time left, 19 t / 3: one tau after it has grown, its heat is 1 / e
    @Test
    void theFireballCoolsDownOnceGrown() {
        Blast SUT = new Blast(new Collision(EARTH, UP, UP, SPEED, BINDING_ENERGY / 8.0), START);
        double riseTime = EARTH_RADIUS / 2.0 / SPEED;

        Flare fireball = SUT.flaresAt(new JulianDate(START.days() + riseTime * (1.0 + 19.0 / 3.0)), Vector3.ZERO)
                .getFirst();

        assertThat(fireball.heat()).isCloseTo(Math.exp(-1.0), withinPercentage(HEAT_PRECISION_PERCENT));
    }

    // A small asteroid makes a fireball of a few metres, grown in a fraction of a second: it glows
    // for ten minutes at least, so that it can be seen when time is slowed down
    @Test
    void theFireballOfASmallImpactGlowsForTenMinutesAtLeast() {
        Blast SUT = new Blast(new Collision(EARTH, UP, UP, SPEED, BINDING_ENERGY * 1e-24), START);

        List<Flare> flares = SUT.flaresAt(new JulianDate(START.days() + NINE_MINUTES), Vector3.ZERO);

        assertThat(flares.getFirst().heat()).isPositive();
    }

    // Ten minutes after the impact the embers are on their way up, all of them above the ground
    @Test
    void theImpactThrowsUpEmbersAboveTheGround() {
        Blast SUT = new Blast(new Collision(EARTH, UP, UP, SPEED, BINDING_ENERGY / 8.0), START);

        List<Flare> flares = SUT.flaresAt(new JulianDate(START.days() + TEN_MINUTES), Vector3.ZERO);

        assertThat(flares.subList(1, flares.size())).hasSize(EMBERS)
                .allSatisfy(ember -> assertThat(ember.position().length()).isGreaterThan(EARTH_RADIUS));
    }

    // Embers may still be flying: the fireball is the only flare with a size
    @Test
    void theFireballIsGoneAfterTwentyTimesItsRiseTime() {
        Blast SUT = new Blast(new Collision(EARTH, UP, UP, SPEED, BINDING_ENERGY / 8.0), START);
        double riseTime = EARTH_RADIUS / 2.0 / SPEED;

        List<Flare> flares = SUT.flaresAt(new JulianDate(START.days() + 21.0 * riseTime), Vector3.ZERO);

        assertThat(flares).allSatisfy(flare -> assertThat(flare.radius()).isZero());
    }
}
