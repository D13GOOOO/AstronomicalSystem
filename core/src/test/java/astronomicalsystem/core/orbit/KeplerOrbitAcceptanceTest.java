package astronomicalsystem.core.orbit;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Tolerances;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.physics.NewtonianGravity;
import astronomicalsystem.core.physics.TimeStep;
import astronomicalsystem.core.physics.VelocityVerlet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Acceptance test: the analytic two-body solution and the numerical N-body integration agree.
 */
class KeplerOrbitAcceptanceTest {

    private static final double G = 2.959122082855911e-4;
    private static final double NO_SOFTENING = 0.0;
    private static final double SUN_MASS = 1.0;
    private static final double PLANET_MASS = 3.003e-6;
    private static final double GRAVITATIONAL_PARAMETER = G * (SUN_MASS + PLANET_MASS);
    private static final OrbitalElements ECCENTRIC_TILTED_ORBIT =
            new OrbitalElements(1.0, new Eccentricity(0.5), 0.3, 1.0, 2.0, 0.7);
    // Velocity Verlet's error after one period is about 1e-6 AU with this many steps
    private static final int STEPS_PER_PERIOD = 10_000;
    private static final double TOLERANCE = 1e-5;

    private final VelocityVerlet integrator = new VelocityVerlet(new NewtonianGravity(G, NO_SOFTENING));

    @Test
    void afterOnePeriodThePlanetIsBackWhereItStarted() {
        StateVector initial = ECCENTRIC_TILTED_ORBIT.stateVector(GRAVITATIONAL_PARAMETER);
        List<Body> sunAndPlanet = List.of(
                new Body(new BodyName("Sun"), BodyKind.STAR, new Mass(SUN_MASS), new Radius(4.65e-3),
                        Vector3.ZERO, Vector3.ZERO),
                new Body(new BodyName("Planet"), BodyKind.PLANET, new Mass(PLANET_MASS), new Radius(4.259e-5),
                        initial.position(), initial.velocity()));

        List<Body> afterOnePeriod = integrateForOnePeriod(sunAndPlanet);

        assertThat(planetRelativeToSun(afterOnePeriod)).usingRecursiveComparison(Tolerances.closeEnough(TOLERANCE))
                .isEqualTo(initial);
    }

    private List<Body> integrateForOnePeriod(List<Body> bodies) {
        double period = 2.0 * Math.PI * Math.sqrt(Math.pow(ECCENTRIC_TILTED_ORBIT.semiMajorAxis(), 3)
                / GRAVITATIONAL_PARAMETER);
        TimeStep step = new TimeStep(period / STEPS_PER_PERIOD);
        List<Body> integrated = bodies;
        for (int i = 0; i < STEPS_PER_PERIOD; i++) {
            integrated = integrator.step(integrated, step);
        }
        return integrated;
    }

    private static StateVector planetRelativeToSun(List<Body> sunAndPlanet) {
        Body sun = sunAndPlanet.get(0);
        Body planet = sunAndPlanet.get(1);
        return new StateVector(planet.position().minus(sun.position()), planet.velocity().minus(sun.velocity()));
    }
}
