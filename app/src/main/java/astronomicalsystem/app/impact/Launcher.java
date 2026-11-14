package astronomicalsystem.app.impact;

import astronomicalsystem.app.camera.CurrentCamera;
import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.follow.Focus;
import astronomicalsystem.app.follow.Following;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.impact.Approach;
import astronomicalsystem.core.impact.Impactor;
import astronomicalsystem.core.simulation.Joinable;
import org.jetbrains.annotations.NotNull;

/**
 * Throws impactors at the body the camera follows, so that the user sees them arrive: from the
 * left of the image, aiming at the side of the target that faces the camera.
 */
public class Launcher implements LaunchControls {

    private final Joinable system;
    private final Following following;
    private final CurrentCamera currentCamera;
    // Numbers the impactors in the names, so that no two are the same
    private int launches;

    public Launcher(@NotNull Joinable system, @NotNull Following following, @NotNull CurrentCamera currentCamera) {
        this.system = system;
        this.following = following;
        this.currentCamera = currentCamera;
    }

    @Override
    public void launch(@NotNull Impactor impactor) {
        switch (following.focus()) {
            case Focus.Barycentre barycentre -> {
                // Nothing to aim at
            }
            case Focus.OnBody(BodyName name) -> launchAt(targetNamed(name), impactor);
        }
    }

    private void launchAt(@NotNull Body target, @NotNull Impactor impactor) {
        Projection camera = currentCamera.camera();
        Approach fromTheLeft = new Approach(camera.rightward().times(-1.0), camera.forward().times(-1.0));
        launches++;
        system.add(impactor.launchedAt(target, fromTheLeft, new BodyName(wordFor(impactor) + " " + launches)));
    }

    // The followed body is always among the bodies of the system
    private @NotNull Body targetNamed(@NotNull BodyName name) {
        return system.bodies().stream().filter(body -> body.name().equals(name)).findFirst().orElseThrow();
    }

    private static @NotNull String wordFor(@NotNull Impactor impactor) {
        return switch (impactor) {
            case ASTEROID -> "Asteroid";
            case PLANETESIMAL -> "Planetesimal";
            case PROTOPLANET -> "Protoplanet";
        };
    }
}
