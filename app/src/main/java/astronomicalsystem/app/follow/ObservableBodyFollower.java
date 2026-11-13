package astronomicalsystem.app.follow;

import astronomicalsystem.app.camera.CameraTarget;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Body follower that notifies its observers every time it follows something else; not when the
 * followed body only moves.
 */
public class ObservableBodyFollower extends BodyFollower implements Observable<Focus> {

    private final List<Observer<Focus>> observers = new ArrayList<>();

    public ObservableBodyFollower(@NotNull CameraTarget camera) {
        super(camera);
    }

    @Override
    public void update(@NotNull Observable<List<Body>> subject, @NotNull List<Body> state) {
        Focus before = focus();
        super.update(subject, state);
        notifyObserversIfChangedFrom(before);
    }

    @Override
    public void followNext() {
        Focus before = focus();
        super.followNext();
        notifyObserversIfChangedFrom(before);
    }

    @Override
    public void followPrevious() {
        Focus before = focus();
        super.followPrevious();
        notifyObserversIfChangedFrom(before);
    }

    @Override
    public void addObserver(@NotNull Observer<Focus> observer) {
        observers.add(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer<Focus> observer : observers) {
            observer.update(this, getState());
        }
    }

    @Override
    public @NotNull Focus getState() {
        return focus();
    }

    private void notifyObserversIfChangedFrom(@NotNull Focus before) {
        if (!focus().equals(before)) {
            notifyObservers();
        }
    }
}
