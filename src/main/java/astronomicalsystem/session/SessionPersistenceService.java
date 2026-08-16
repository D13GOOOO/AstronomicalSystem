package astronomicalsystem.session;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import astronomicalsystem.model.AstronomicalSystem;
import astronomicalsystem.model.CelestialBody;

/**
 * A dedicated service responsible for handling all file operations related to saving and loading 
 * the simulation. It manages the complex process of converting the entire living state of the universe, 
 * including the physics engine, the camera angles, and the user interface settings, into a compact binary 
 * format that can be stored securely on the computer's hard drive.
 * 
 * One of the most important features of this service is its intelligent backward compatibility. Software 
 * evolves over time, and older versions of this application used entirely different formats to save their 
 * progress. Instead of crashing when a user attempts to load an old save file, this service actively 
 * inspects the loaded data, recognizes outdated formats, and seamlessly upgrades them into the modern 
 * session structure before handing them over to the main application.
 */
public class SessionPersistenceService {

    /**
     * Packages a comprehensive snapshot of the entire simulation and writes it to a binary file on the disk. 
     * This captures absolutely everything needed to perfectly resume the experience later, from the exact 
     * mathematical coordinates of every planet to the angle the user was looking from.
     *
     * @param filename     the exact string path and name of the file to be created or overwritten on the hard drive.
     * @param sessionState the complete bundled package containing the physics engine, camera data, and user interface state.
     * @throws IOException if the system encounters a hardware error, such as lacking permission to write to the folder or running out of disk space.
     */
    public void save(String filename, SessionState sessionState) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(sessionState);
        }
    }

    /**
     * Opens a previously saved binary file, reads its contents, and meticulously reconstructs the simulation 
     * state so the user can resume exactly where they left off. 
     * 
     * This method contains an advanced safety net for backward compatibility. When it reads the file, it checks 
     * the shape of the data. If it finds a modern, fully featured session state, it simply loads it. If it finds 
     * a legacy save file from a previous software version that only contained the physics engine, or an even older 
     * version that only contained a raw list of planets, it catches this discrepancy. Instead of crashing the 
     * application, it automatically generates a modern session wrapper around the old physics data, supplying 
     * safe default values for the missing camera and interface settings.
     *
     * @param filename the exact string path and name of the file to open and read.
     * @return a fully reconstructed, modern session state ready to be injected directly into the main application loop.
     * @throws IOException            if the file cannot be located, is corrupted, or contains an entirely alien data structure that cannot be translated.
     * @throws ClassNotFoundException if the loaded file contains objects belonging to a version of the software that no longer exists and cannot be mapped to current code.
     */
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
                        throw new IOException("Unsupported body type in legacy save payload: " + body.getClass().getName());
                    }
                    loadedSystem.addBody(celestialBody);
                }

                return SessionState.legacy(loadedSystem);
            }

            throw new IOException("Unsupported save format. Root object type: " + snapshot.getClass().getName());
        }
    }
}