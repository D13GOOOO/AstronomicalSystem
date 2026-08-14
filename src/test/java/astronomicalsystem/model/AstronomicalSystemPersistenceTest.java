package astronomicalsystem.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class AstronomicalSystemPersistenceTest {

    @TempDir
    Path tempDir;

    @Test
    void saveAndLoadRestorePhysicsBodies() throws Exception {
        AstronomicalSystem system = new AstronomicalSystem();
        system.addBody(new Planet("Earth", 50.0, 1.0, new Point3D(1.0, 2.0, 3.0)));
        system.addBody(new Star("Sun", 1000.0, 5.0, Point3D.ZERO));

        Path saveFile = tempDir.resolve("system.dat");
        system.saveToFile(saveFile.toString());

        AstronomicalSystem restored = new AstronomicalSystem();
        restored.loadFromFile(saveFile.toString());

        List<CelestialBody> bodies = restored.getBodies();
        assertEquals(2, bodies.size());
        
        CelestialBody earth = bodies.get(0);
        assertInstanceOf(Planet.class, earth);
        assertEquals("Earth", earth.getName());
        assertEquals(50.0, earth.getMass(), 1e-9);
        assertEquals(new Point3D(1.0, 2.0, 3.0), earth.getPosition());
        
        CelestialBody sun = bodies.get(1);
        assertInstanceOf(Star.class, sun);
        assertEquals("Sun", sun.getName());
        assertEquals(1000.0, sun.getMass(), 1e-9);
        assertEquals(Point3D.ZERO, sun.getPosition());
    }
}