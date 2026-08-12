package astronomicalsystem.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AstronomicalSystemPersistenceTest {

    @TempDir
    Path tempDir;

    @Test
    void saveAndLoadRestorePhysicsBodies() throws Exception {
        AstronomicalSystem system = new AstronomicalSystem();
        system.addBody(new Planet("Earth", 50.0, 1.0, new Point3D(1.0, 2.0, 3.0)));
        system.addBody(new Star("Sun", 1000.0, 5.0, new Point3D(0.0, 0.0, 0.0)));

        Path saveFile = tempDir.resolve("system.dat");
        system.saveToFile(saveFile.toString());

        AstronomicalSystem restored = new AstronomicalSystem();
        restored.loadFromFile(saveFile.toString());

        assertEquals(2, restored.getBodies().size());
        assertEquals("Earth", restored.getBodies().get(0).getName());
        assertEquals("Sun", restored.getBodies().get(1).getName());
    }
}
