package astronomicalsystem.app.camera;

import astronomicalsystem.app.view.ClipPoint;
import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

/**
 * Perspective camera that looks at a target from an eye, with a given direction pointing up in the
 * image.
 */
public final class Camera {

    public Camera(@NotNull Vector3 eye, @NotNull Vector3 target, @NotNull Vector3 up, @NotNull Lens lens,
                  @NotNull Viewport viewport) {
    }

    /**
     * @return where the point lands in the clip coordinates of OpenGL
     */
    public @NotNull ClipPoint clipPoint(@NotNull Vector3 point) {
        return new ClipPoint(0.0, 0.0, 0.0, 0.0);
    }
}
