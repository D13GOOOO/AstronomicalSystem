package astronomicalsystem.app.follow;

import astronomicalsystem.app.camera.CameraTarget;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import org.jetbrains.annotations.NotNull;

public class ObservableBodyFollower extends BodyFollower implements Observable<Focus> {

    public ObservableBodyFollower(@NotNull CameraTarget camera) {
        super(camera);
    }

    @Override
    public void addObserver(@NotNull Observer<Focus> observer) {
    }

    @Override
    public void notifyObservers() {
    }

    @Override
    public @NotNull Focus getState() {
        return focus();
    }
}
