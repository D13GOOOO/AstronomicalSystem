package astronomicalsystem.app.camera;

import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

/**
 * Camera that orbits a fixed target, with the ecliptic north up in the image: its position, its
 * lens and the size of the image it fills.
 */
public class CameraRig {

    private static final Vector3 ECLIPTIC_NORTH = new Vector3(0.0, 0.0, 1.0);

    private final Vector3 target;
    private Orbit orbit;
    private final Lens lens;
    private final Viewport viewport;

    public CameraRig(@NotNull Vector3 target, @NotNull Orbit orbit, @NotNull Lens lens, @NotNull Viewport viewport) {
        this.target = target;
        this.orbit = orbit;
        this.lens = lens;
        this.viewport = viewport;
    }

    public @NotNull Projection camera() {
        return new Camera(orbit.eye(target), target, ECLIPTIC_NORTH, lens, viewport);
    }

    /**
     * Moves the camera around the target.
     *
     * @param azimuthAngle   in radians; precondition: finite
     * @param elevationAngle in radians, up to the limit of the orbit; precondition: finite
     */
    public void orbitBy(double azimuthAngle, double elevationAngle) {
        orbit = orbit.rotated(azimuthAngle, elevationAngle);
    }

    /**
     * Moves the camera towards or away from the target.
     *
     * @param factor multiplies the distance, up to the limits of the orbit; precondition: positive and
     *               finite
     */
    public void zoomBy(double factor) {
        orbit = orbit.zoomed(factor);
    }

    /**
     * Fits the image to a new size, for example after the window has been resized.
     */
    public void resize(@NotNull Viewport newViewport) {
    }
}
