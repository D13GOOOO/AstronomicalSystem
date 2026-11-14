package astronomicalsystem.app.camera;

import org.jetbrains.annotations.NotNull;

/**
 * Gives the camera as it is now.
 */
public interface CurrentCamera {

    /**
     * @return the current camera, the same object until it changes
     */
    @NotNull Projection camera();
}
