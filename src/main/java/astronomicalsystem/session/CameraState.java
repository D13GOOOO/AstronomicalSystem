package astronomicalsystem.session;

import java.io.Serial;
import java.io.Serializable;

/**
 * A highly optimized, immutable data structure that captures the exact physical position and viewing 
 * angles of the three dimensional camera at a specific moment in time. 
 * 
 * By saving these precise coordinates, the simulation can perfectly recreate the exact viewport and 
 * perspective the user was looking through when they saved their session. Because it is implemented 
 * as a record, the data is completely read only and cannot be modified once it is created. This ensures 
 * absolute data safety and prevents any accidental modification during the complex process of writing 
 * the simulation state to a binary save file on the hard drive.
 *
 * @param translateX the physical position of the camera along the horizontal axis, determining how far left or right the user has panned their view.
 * @param translateY the physical position of the camera along the vertical axis, determining how far up or down the user has panned their view.
 * @param translateZ the physical position of the camera along the depth axis, acting as the zoom level to determine how close or far the camera is from the center of the universe.
 * @param rotateX    the vertical tilt or pitch angle of the camera, representing its rotation up or down around the horizontal axis.
 * @param rotateY    the horizontal swivel or yaw angle of the camera, representing its rotation left or right around the vertical axis.
 */
public record CameraState(
        double translateX,
        double translateY,
        double translateZ,
        double rotateX,
        double rotateY
) implements Serializable {

    /**
     * The serialization identifier ensuring that saved camera configurations remain perfectly compatible 
     * with the current version of the application code when loading a saved session back into memory.
     */
    @Serial
    private static final long serialVersionUID = 1L;
}