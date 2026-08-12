package astronomicalsystem.session;

import astronomicalsystem.model.AstronomicalSystem;
import astronomicalsystem.model.CelestialBody;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Handles persistence of application session snapshots.
 */
public class SessionPersistenceService {

    public void save(String filename, SessionState sessionState) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(sessionState);
        }
    }

    public SessionState load(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            Object snapshot = in.readObject();

            if (snapshot instanceof SessionState sessionState) {
                return sessionState;
            }

            if (snapshot instanceof AstronomicalSystem loadedSystem) {
                return SessionState.legacy(loadedSystem);
            }

            if (snapshot instanceof List<?> rawBodies) {
                AstronomicalSystem loadedSystem = new AstronomicalSystem();

                for (Object body : rawBodies) {
                    if (!(body instanceof CelestialBody celestialBody)) {
                        throw new IOException("Unsupported body type in legacy save: " + body.getClass().getName());
                    }

                    loadedSystem.addBody(celestialBody);
                }

                return SessionState.legacy(loadedSystem);
            }

            throw new IOException("Unsupported save format: " + snapshot.getClass().getName());
        }
    }
}