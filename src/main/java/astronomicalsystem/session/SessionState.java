package astronomicalsystem.session;

import astronomicalsystem.model.AstronomicalSystem;

import java.io.Serial;
import java.io.Serializable;

/**
 * Full application session snapshot, including simulation model and UI state.
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

    public static SessionState current(AstronomicalSystem system, boolean paused, int customPlanetCount, CameraState cameraState) {
        return new SessionState(system, paused, customPlanetCount, cameraState, false);
    }

    public static SessionState legacy(AstronomicalSystem system) {
        return new SessionState(system, false, 1, null, true);
    }
}