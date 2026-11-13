package astronomicalsystem.app.follow;

import astronomicalsystem.app.camera.CameraTarget;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Keeps the camera on what the user chose to follow, every time the bodies move.
 */
public class BodyFollower implements Observer<List<Body>>, FollowControls {

    // The frame of the simulation is centred on the barycentre of the solar system
    private static final Vector3 BARYCENTRE = Vector3.ZERO;

    private final CameraTarget camera;

    public BodyFollower(@NotNull CameraTarget camera) {
        this.camera = camera;
    }

    public @NotNull Focus focus() {
        return Focus.BARYCENTRE;
    }

    @Override
    public void update(@NotNull Observable<List<Body>> subject, @NotNull List<Body> state) {
        camera.lookAt(BARYCENTRE);
    }

    @Override
    public void followNext() {
    }

    @Override
    public void followPrevious() {
    }
}
