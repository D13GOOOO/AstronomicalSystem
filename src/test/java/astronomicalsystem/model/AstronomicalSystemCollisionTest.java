package astronomicalsystem.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class AstronomicalSystemCollisionTest {

    @Test
    void simulateCollapsesChainOfOverlappingBodiesInOneTick() {
        AstronomicalSystem system = new AstronomicalSystem();

        system.addBody(new Planet("A", 1.0, 1.0, new Point3D(0.0, 0.0, 0.0)));
        system.addBody(new Planet("B", 2.0, 1.0, new Point3D(0.0, 0.0, 0.0)));
        system.addBody(new Planet("C", 3.0, 1.0, new Point3D(0.0, 0.0, 0.0)));

        system.simulate(1);

        assertEquals(1, system.getBodies().size());
        assertEquals(6.0, system.getBodies().get(0).getMass(), 1e-9);
    }

    @Test
    void anchoredCollisionPreservesStationaryRole() {
        AstronomicalSystem system = new AstronomicalSystem();

        system.addBody(new Star("Sun", 10.0, 5.0, new Point3D(0.0, 0.0, 0.0)));
        system.addBody(new Planet("Planet", 2.0, 1.0, new Point3D(0.0, 0.0, 0.0)));

        system.simulate(1);

        assertEquals(1, system.getBodies().size());
        assertInstanceOf(Star.class, system.getBodies().get(0));
        assertEquals(12.0, system.getBodies().get(0).getMass(), 1e-9);
    }
}
