package astronomicalsystem.session;

import java.io.Serial;
import java.io.Serializable;

/**
 * A highly optimized, immutable snapshot of the JavaFX runtime camera transform.
 * <p>
 * This record captures the precise spatial and rotational data required to 
 * perfectly reconstruct the user's viewport upon loading a saved session.
 * </p>
 *
 * @param translateX the camera's position along the X-axis (horizontal pan).
 * @param translateY the camera's position along the Y-axis (vertical pan).
 * @param translateZ the camera's position along the Z-axis (zoom level).
 * @param rotateX    the camera's pitch angle (rotation around the X-axis).
 * @param rotateY    the camera's yaw angle (rotation around the Y-axis).
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