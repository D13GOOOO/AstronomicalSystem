package astronomicalsystem.session;

import java.io.Serial;
import java.io.Serializable;

/**
 * Serializable snapshot of the runtime camera transform.
 */
public record CameraState(
        double translateX,
        double translateY,
        double translateZ,
        double rotateX,
        double rotateY
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}