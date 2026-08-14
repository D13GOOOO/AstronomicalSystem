package astronomicalsystem.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MoonTest {

    @Test
    void moonDoesNotImplementKinematic() {
        Planet host = new Planet("Host", 100.0, 1.0, new Point3D(10.0, 20.0, 30.0));
        Moon moon = new Moon("Moon", 1.0, 1.0, host, 5.0, Math.PI / 2.0);

        assertFalse(moon instanceof Kinematic);
    }

    @Test
    void moonFollowsHostPositionWhenOrbitUpdates() {
        Planet host = new Planet("Host", 100.0, 1.0, new Point3D(10.0, 20.0, 30.0));
        Moon moon = new Moon("Moon", 1.0, 1.0, host, 5.0, Math.PI / 2.0);

        host.setPosition(new Point3D(100.0, 200.0, 300.0));
        moon.updateOrbit();

        assertEquals(100.0, moon.getPosition().x(), 1e-9);
        assertEquals(200.0, moon.getPosition().y(), 1e-9);
        assertEquals(305.0, moon.getPosition().z(), 1e-9);
    }
}