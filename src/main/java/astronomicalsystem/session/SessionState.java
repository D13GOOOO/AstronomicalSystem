package astronomicalsystem.session;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import astronomicalsystem.model.AstronomicalSystem;

/**
 * A highly optimized, immutable data structure representing a complete snapshot of the application 
 * at a specific moment in time. This record serves as the ultimate memory container, capturing both 
 * the deep mathematical state of the physics engine and the volatile settings of the graphical user 
 * interface. 
 * 
 * It acts perfectly as the Memento in the Memento design pattern, freezing the entire simulation 
 * so it can be safely written to the hard drive. Because it is an immutable record, there is absolutely 
 * no risk of the physics engine updating a planet's position halfway through the saving process, 
 * which would otherwise result in a corrupted save file.
 *
 * @param system            the central physics engine containing all the celestial bodies and their ongoing gravitational calculations.
 * @param paused            a flag capturing whether the user had temporarily stopped the flow of time when the save was initiated.
 * @param customPlanetCount the numerical tracker used to generate sequential names for any newly created dynamic planets.
 * @param cameraState       the exact spatial coordinates and rotation angles of the user perspective, ensuring they are looking at the exact same spot upon reloading.
 * @param legacyPhysicsOnly a specific marker indicating whether this snapshot was artificially reconstructed from a much older version of the software that only knew how to save planetary data.
 */
public record SessionState(
        AstronomicalSystem system,
        boolean paused,
        int customPlanetCount,
        CameraState cameraState,
        boolean legacyPhysicsOnly
) implements Serializable {

    /**
     * The serialization identifier ensuring that saved simulation files containing session snapshots 
     * remain fully compatible with the current version of the application code when they are loaded 
     * back into memory.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * A dedicated factory method used to bundle a standard, modern session snapshot. It takes the 
     * current living state of both the physics engine and the user interface and permanently seals 
     * them into this immutable record, preparing them to be written to the disk.
     *
     * @param system            the living physics engine to be preserved. Must never be null.
     * @param paused            the current status of the flow of time in the simulation.
     * @param customPlanetCount the numerical tracker used for naming newly spawned planets.
     * @param cameraState       the exact perspective the user is currently viewing the universe from. This can safely be null if the application is running in a purely mathematical testing environment without a graphical interface.
     * @return a brand new, fully populated session state representing the exact current moment in the simulation.
     */
    public static SessionState current(AstronomicalSystem system, boolean paused, int customPlanetCount, CameraState cameraState) {
        Objects.requireNonNull(system, "Cannot create a session state with a null AstronomicalSystem");
        return new SessionState(system, paused, customPlanetCount, cameraState, false);
    }

    /**
     * A specialized factory method designed to safely reconstruct a valid session state from outdated 
     * save files. Older iterations of this application only knew how to save the mathematical physics 
     * model, completely ignoring camera angles or interface menus. 
     * 
     * To prevent the modern application from crashing when loading these old files, this adapter takes 
     * the loaded legacy physics engine and carefully wraps it with safe, default user interface settings, 
     * such as unpausing the game and resetting the camera to the center of the universe.
     *
     * @param system the legacy physics engine that was successfully recovered from the old save file. Must never be null.
     * @return a completely backwards compatible session state that the modern application can read and run without crashing.
     */
    public static SessionState legacy(AstronomicalSystem system) {
        Objects.requireNonNull(system, "Cannot reconstruct a legacy session state with a null AstronomicalSystem");
        return new SessionState(system, false, 1, null, true);
    }
}