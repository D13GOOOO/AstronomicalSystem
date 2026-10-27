package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;

/**
 * Acceptance test of the integrator together with the real gravity model.
 */
class VelocityVerletOrbitTest {

    private static final double G = 2.959122082855911e-4;
    private static final double NO_SOFTENING = 0.0;
    private static final double SUN_MASS = 1.0;
    private static final double EARTH_MASS = 3.003e-6;
    private static final double EARTH_DISTANCE = 1.0;
    private static final TimeStep ONE_DAY = new TimeStep(1.0);
    private static final int DAYS_IN_A_YEAR = 365;
    private static final double ENERGY_TOLERANCE_PERCENT = 1e-4;

    private final VelocityVerlet SUT = new VelocityVerlet(new NewtonianGravity(G, NO_SOFTENING));

    @Test
    void aCircularOrbitConservesEnergyOverAYear() {
        Body sun = new Body(new BodyName("Sun"), BodyKind.STAR, new Mass(SUN_MASS), new Radius(4.65e-3),
                Vector3.ZERO, Vector3.ZERO);
        Body earth = new Body(new BodyName("Earth"), BodyKind.PLANET, new Mass(EARTH_MASS), new Radius(4.259e-5),
                new Vector3(EARTH_DISTANCE, 0.0, 0.0),
                new Vector3(0.0, Math.sqrt(G * SUN_MASS / EARTH_DISTANCE), 0.0));
        List<Body> initial = List.of(sun, earth);

        List<Body> afterAYear = stepDaily(initial, DAYS_IN_A_YEAR);

        assertThat(totalEnergy(afterAYear))
                .isCloseTo(totalEnergy(initial), withinPercentage(ENERGY_TOLERANCE_PERCENT));
    }

    private List<Body> stepDaily(List<Body> bodies, int days) {
        List<Body> stepped = bodies;
        for (int day = 0; day < days; day++) {
            stepped = SUT.step(stepped, ONE_DAY);
        }
        return stepped;
    }

    private static double totalEnergy(List<Body> sunAndEarth) {
        Body sun = sunAndEarth.get(0);
        Body earth = sunAndEarth.get(1);
        double distance = earth.position().minus(sun.position()).length();
        return kineticEnergy(sun) + kineticEnergy(earth) - G * SUN_MASS * EARTH_MASS / distance;
    }

    private static double kineticEnergy(Body body) {
        return 0.5 * body.mass().solarMasses() * body.velocity().lengthSquared();
    }
}
