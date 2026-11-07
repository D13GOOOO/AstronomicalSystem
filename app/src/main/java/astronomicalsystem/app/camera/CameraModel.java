package astronomicalsystem.app.camera;

import org.jetbrains.annotations.NotNull;

/**
 * Operations the user can perform on the camera.
 */
public interface CameraModel {

    /**
     * Moves the camera around its target.
     *
     * @param azimuthAngle   in radians; precondition: finite
     * @param elevationAngle in radians, up to the limit of the orbit; precondition: finite
     */
    void orbitBy(double azimuthAngle, double elevationAngle);

    /**
     * Moves the camera towards or away from its target.
     *
     * @param factor multiplies the distance, up to the limits of the orbit; precondition: positive and
     *               finite
     */
    void zoomBy(double factor);

    /**
     * Fits the image to a new size, for example after the window has been resized.
     */
    void resize(@NotNull Viewport newViewport);
}
