package astronomicalsystem.model;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AstronomicalSystemCollisionTest {

    @Test
    void simulateCollapsesChainOfOverlappingBodiesInOneTick() {
        AstronomicalSystem system = new AstronomicalSystem();

        system.addBody(new Planet("A", 1.0, 1.0, Point3D.ZERO));
        system.addBody(new Planet("B", 2.0, 1.0, Point3D.ZERO));
        system.addBody(new Planet("C", 3.0, 1.0, Point3D.ZERO));

        system.simulate(1);

        List<CelestialBody> bodies = system.getBodies();
        assertEquals(1, bodies.size());
        
        CelestialBody merged = bodies.get(0);
        assertEquals(6.0, merged.getMass(), 1e-9);
        assertTrue(merged.getName().contains("+"));
    }

    @Test
    void anchoredCollisionPreservesStationaryRole() {
        AstronomicalSystem system = new AstronomicalSystem();

        system.addBody(new Star("Sun", 10.0, 5.0, Point3D.ZERO));
        system.addBody(new Planet("Planet", 2.0, 1.0, Point3D.ZERO));

        system.simulate(1);

        List<CelestialBody> bodies = system.getBodies();
        assertEquals(1, bodies.size());
        
        CelestialBody merged = bodies.get(0);
        assertInstanceOf(Star.class, merged);
        assertEquals(12.0, merged.getMass(), 1e-9);
        assertEquals("Sun+", merged.getName());
        assertEquals(Point3D.ZERO, merged.getPosition());
    }
}