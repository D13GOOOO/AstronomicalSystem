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
 * Handles the binary serialization and deserialization of application session snapshots.
 * <p>
 * This service ensures robust I/O operations and provides seamless backward compatibility 
 * for loading obsolete save file formats (e.g., loading just the raw body list or the naked system).
 * </p>
 */
public class SessionPersistenceService {

    /**
     * Serializes the entire session state to a binary file on disk.
     *
     * @param filename     the target file path.
     * @param sessionState the comprehensive snapshot to save.
     * @throws IOException if the file cannot be created, opened, or written to.
     */
    public void save(String filename, SessionState sessionState) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(sessionState);
        }
    }

    /**
     * Reads and reconstructs a session state from a binary file.
     * <p>
     * <b>Backward Compatibility:</b> This method dynamically inspects the loaded object type.
     * It natively supports the modern {@link SessionState}, but will intelligently wrap 
     * older formats (like a bare {@link AstronomicalSystem} or a {@code List<CelestialBody>}) 
     * into a new legacy snapshot to prevent application crashes on outdated saves.
     * </p>
     *
     * @param filename the source file path to read from.
     * @return a valid, fully reconstructed {@link SessionState}.
     * @throws IOException            if the file cannot be found or read, or if the format is fundamentally incompatible.
     * @throws ClassNotFoundException if the serialized payload references classes that no longer exist.
     */
    public SessionState load(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            Object snapshot = in.readObject();

            // 1. Modern Format: The full session container
            if (snapshot instanceof SessionState sessionState) {
                return sessionState;
            }

            // 2. Legacy Format A: Only the AstronomicalSystem was saved
            if (snapshot instanceof AstronomicalSystem loadedSystem) {
                return SessionState.legacy(loadedSystem);
            }

            // 3. Legacy Format B: Only the raw list of celestial bodies was saved
            if (snapshot instanceof List<?> rawBodies) {
                AstronomicalSystem loadedSystem = new AstronomicalSystem();

                for (Object body : rawBodies) {
                    if (!(body instanceof CelestialBody celestialBody)) {
                        throw new IOException("Unsupported body type in legacy save payload: " + body.getClass().getName());
                    }
                    loadedSystem.addBody(celestialBody);
                }

                return SessionState.legacy(loadedSystem);
            }

            // Fallback for completely unrecognized payloads
            throw new IOException("Unsupported save format. Root object type: " + snapshot.getClass().getName());
        }
    }
}