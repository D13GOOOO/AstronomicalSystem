package astronomicalsystem.app.camera;

import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

/**
 * What the camera looks at.
 */
public interface CameraTarget {

    /**
     * Moves the camera with its target, so that it sees the new target from the same distance and
     * direction.
     */
    void lookAt(@NotNull Vector3 target);

    /**
     * Moves the camera with its target, as {@link #lookAt(Vector3)}, and keeps it at least as far
     * from it as the clearance, for example outside the body it follows.
     *
     * @param clearance in AU; precondition: positive and finite
     */
    void lookAt(@NotNull Vector3 target, double clearance);
}
