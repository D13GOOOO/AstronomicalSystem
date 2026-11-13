package astronomicalsystem.app.presenter;

import astronomicalsystem.app.follow.Focus;
import astronomicalsystem.app.view.TextView;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import org.jetbrains.annotations.NotNull;

public class FocusPresenter implements Observer<Focus> {

    public FocusPresenter(@NotNull TextView view) {
    }

    @Override
    public void update(@NotNull Observable<Focus> subject, @NotNull Focus state) {
    }
}
