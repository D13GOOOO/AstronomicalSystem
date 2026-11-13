package astronomicalsystem.app.camera;

import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

/**
 * Camera that orbits a fixed target, with the ecliptic north up in the image: its position, its
 * lens and the size of the image it fills.
 */
public class CameraRig implements CameraModel, CameraTarget {

    private static final Vector3 ECLIPTIC_NORTH = new Vector3(0.0, 0.0, 1.0);

    private final Vector3 target;
    private final Lens lens;
    private Orbit orbit;
    private Viewport viewport;
    // Rebuilt only when the orbit or the viewport changes: the same object means the same picture
    private Projection camera;

    public CameraRig(@NotNull Vector3 target, @NotNull Orbit orbit, @NotNull Lens lens, @NotNull Viewport viewport) {
        this.target = target;
        this.lens = lens;
        this.orbit = orbit;
        this.viewport = viewport;
        this.camera = cameraOf(orbit, viewport);
    }

    /**
     * @return the current camera, the same object until the rig changes
     */
    public @NotNull Projection camera() {
        return camera;
    }

    @Override
    public void orbitBy(double azimuthAngle, double elevationAngle) {
        change(orbit.rotated(azimuthAngle, elevationAngle), viewport);
    }

    @Override
    public void zoomBy(double factor) {
        change(orbit.zoomed(factor), viewport);
    }

    @Override
    public void resize(@NotNull Viewport newViewport) {
        change(orbit, newViewport);
    }

    @Override
    public void lookAt(@NotNull Vector3 newTarget) {
    }

    private void change(@NotNull Orbit newOrbit, @NotNull Viewport newViewport) {
        if (newOrbit.equals(orbit) && newViewport.equals(viewport)) {
            return;
        }
        orbit = newOrbit;
        viewport = newViewport;
        camera = cameraOf(newOrbit, newViewport);
    }

    private @NotNull Projection cameraOf(@NotNull Orbit someOrbit, @NotNull Viewport someViewport) {
        return new Camera(someOrbit.eye(target), target, ECLIPTIC_NORTH, lens, someViewport);
    }
}
