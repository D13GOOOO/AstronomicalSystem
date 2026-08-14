package astronomicalsystem.session;

import astronomicalsystem.model.AstronomicalSystem;
import astronomicalsystem.model.Point3D;
import astronomicalsystem.model.Planet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionPersistenceServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void saveAndLoadRoundTripSessionState() throws Exception {
        SessionPersistenceService service = new SessionPersistenceService();
        AstronomicalSystem system = new AstronomicalSystem();
        system.addBody(new Planet("Earth", 50.0, 1.0, new Point3D(1.0, 2.0, 3.0)));

        SessionState sessionState = SessionState.current(
                system,
                true,
                7,
                new CameraState(10.0, 20.0, -3000.0, 15.0, 25.0)
        );

        Path saveFile = tempDir.resolve("session.dat");
        service.save(saveFile.toString(), sessionState);

        SessionState restored = service.load(saveFile.toString());

        assertFalse(restored.legacyPhysicsOnly());
        assertTrue(restored.paused());
        assertEquals(7, restored.customPlanetCount());
        assertEquals(1, restored.system().getBodies().size());
        assertEquals("Earth", restored.system().getBodies().get(0).getName());
        assertEquals(10.0, restored.cameraState().translateX(), 1e-9);
        assertEquals(20.0, restored.cameraState().translateY(), 1e-9);
        assertEquals(-3000.0, restored.cameraState().translateZ(), 1e-9);
        assertEquals(15.0, restored.cameraState().rotateX(), 1e-9);
        assertEquals(25.0, restored.cameraState().rotateY(), 1e-9);
    }

    @Test
    void loadWrapsLegacyPhysicsOnlySave() throws Exception {
        SessionPersistenceService service = new SessionPersistenceService();
        AstronomicalSystem system = new AstronomicalSystem();
        system.addBody(new Planet("Sun", 1000.0, 1.0, Point3D.ZERO));

        Path saveFile = tempDir.resolve("legacy.dat");
        system.saveToFile(saveFile.toString());

        SessionState restored = service.load(saveFile.toString());

        assertTrue(restored.legacyPhysicsOnly());
        assertEquals(1, restored.system().getBodies().size());
        assertEquals("Sun", restored.system().getBodies().get(0).getName());
    }
}