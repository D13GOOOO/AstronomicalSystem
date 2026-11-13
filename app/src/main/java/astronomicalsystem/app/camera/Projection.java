package astronomicalsystem.app.camera;

import astronomicalsystem.app.view.ClipPoint;
import astronomicalsystem.app.view.ImagePoint;
import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

/**
 * Maps the space of the simulation onto the image.
 */
public interface Projection {

    /**
     * @return where the point lands in the clip coordinates of OpenGL
     */
    @NotNull ClipPoint clipPoint(@NotNull Vector3 point);

    /**
     * @param size   length across the line of sight, in AU
     * @param centre clip point of the middle of the length; precondition: in front of the camera
     * @return how many pixels the length spans in the image
     */
    double apparentSizeInPixels(double size, @NotNull ClipPoint centre);

    /**
     * @param point precondition: in front of the camera
     * @return where the point is in the image, also when it falls outside it
     */
    @NotNull ImagePoint imagePoint(@NotNull ClipPoint point);
}
