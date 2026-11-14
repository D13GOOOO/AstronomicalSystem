package astronomicalsystem.app.impact;

import astronomicalsystem.app.camera.CurrentCamera;
import astronomicalsystem.app.follow.Following;
import astronomicalsystem.core.impact.Impactor;
import astronomicalsystem.core.simulation.Joinable;
import org.jetbrains.annotations.NotNull;

/**
 * Throws impactors at the body the camera follows, so that the user sees them arrive.
 */
public class Launcher implements LaunchControls {

    private final Joinable system;
    private final Following following;
    private final CurrentCamera currentCamera;

    public Launcher(@NotNull Joinable system, @NotNull Following following, @NotNull CurrentCamera currentCamera) {
        this.system = system;
        this.following = following;
        this.currentCamera = currentCamera;
    }

    @Override
    public void launch(@NotNull Impactor impactor) {
    }
}
