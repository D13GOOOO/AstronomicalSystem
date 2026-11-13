package astronomicalsystem.app.follow;

import astronomicalsystem.app.camera.CameraTarget;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BodyFollower implements Observer<List<Body>>, FollowControls {

    public BodyFollower(@NotNull CameraTarget camera) {
    }

    public @NotNull Focus focus() {
        return Focus.BARYCENTRE;
    }

    @Override
    public void update(@NotNull Observable<List<Body>> subject, @NotNull List<Body> state) {
    }

    @Override
    public void followNext() {
    }

    @Override
    public void followPrevious() {
    }
}
