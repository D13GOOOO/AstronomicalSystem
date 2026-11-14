package astronomicalsystem.app.camera;

import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

/**
 * What the camera looks at.
 */
public interface CameraTarget {

    /**
     * Moves the camera with its target, so that it sees the new target from the same direction and
     * distance, unless it is nearer than the clearance: then it backs off to it, for example to stay
     * outside the body it follows. Zooming in stops at the clearance too.
     *
     * @param clearance in AU; precondition: positive and finite
     */
    void lookAt(@NotNull Vector3 target, double clearance);
}
