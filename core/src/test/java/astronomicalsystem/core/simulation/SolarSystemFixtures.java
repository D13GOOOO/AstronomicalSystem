package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.physics.TimeStep;

import java.util.List;

/**
 * Bodies and time step shared by the simulation tests.
 */
final class SolarSystemFixtures {

    static final TimeStep TWO_DAYS = new TimeStep(2.0);
    static final Body SUN = new Body(new BodyName("Sun"), BodyKind.STAR, new Mass(1.0),
            new Radius(4.65e-3), Vector3.ZERO, Vector3.ZERO);
    static final Body EARTH = new Body(new BodyName("Earth"), BodyKind.PLANET, new Mass(3.003e-6),
            new Radius(4.259e-5), new Vector3(1.0, 0.0, 0.0), new Vector3(0.0, 0.0172, 0.0));
    static final Body MOVED_EARTH = EARTH.withMotion(new Vector3(0.0, 1.0, 0.0),
            new Vector3(-0.0172, 0.0, 0.0));
    static final List<Body> INITIAL_BODIES = List.of(SUN, EARTH);
    static final List<Body> INTEGRATED_BODIES = List.of(SUN, MOVED_EARTH);

    private SolarSystemFixtures() {
    }
}
