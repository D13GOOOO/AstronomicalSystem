package astronomicalsystem.session;

import astronomicalsystem.model.AstronomicalSystem;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * An immutable data transfer object representing a full application session snapshot.
 * <p>
 * This class captures both the deep physical state of the simulation (the N-body model) 
 * and the volatile UI state (camera position, pause status, creator panel counters).
 * It acts as the "Memento" in the Memento design pattern.
 * </p>
 *
 * @param system            the core physical simulation engine and its bodies.
 * @param paused            whether the simulation was paused at the time of saving.
 * @param customPlanetCount the current counter for spawned custom planets (used for naming).
 * @param cameraState       the exact spatial transform of the user's camera.
 * @param legacyPhysicsOnly flag indicating if this state was reconstructed from an older, physics-only save format.
 */
public record SessionState(
        AstronomicalSystem system,
        boolean paused,
        int customPlanetCount,
        CameraState cameraState,
        boolean legacyPhysicsOnly
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Factory method to encapsulate a standard, modern session snapshot.
     *
     * @param system            the core simulation engine (must not be null).
     * @param paused            the current pause state.
     * @param customPlanetCount the counter for dynamically created celestial bodies.
     * @param cameraState       the current camera viewport snapshot (can be null if running headless/tests).
     * @return a new, fully populated {@link SessionState}.
     */
    public static SessionState current(AstronomicalSystem system, boolean paused, int customPlanetCount, CameraState cameraState) {
        Objects.requireNonNull(system, "Cannot create a session state with a null AstronomicalSystem");
        return new SessionState(system, paused, customPlanetCount, cameraState, false);
    }

    /**
     * Factory method to reconstruct a valid session state from legacy save files.
     * <p>
     * Older versions of the application only serialized the physical model. This adapter 
     * wraps the old model with safe UI defaults (e.g., unpaused, reset camera).
     * </p>
     *
     * @param system the loaded legacy simulation engine (must not be null).
     * @return a backwards-compatible {@link SessionState}.
     */
    public static SessionState legacy(AstronomicalSystem system) {
        Objects.requireNonNull(system, "Cannot reconstruct a legacy session state with a null AstronomicalSystem");
        return new SessionState(system, false, 1, null, true);
    }
}