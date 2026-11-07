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
    private final Orbit orbit;
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
}
