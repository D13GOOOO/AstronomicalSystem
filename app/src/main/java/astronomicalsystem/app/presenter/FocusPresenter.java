package astronomicalsystem.app.presenter;

import astronomicalsystem.app.follow.Focus;
import astronomicalsystem.app.view.TextView;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Tells the user what the camera follows.
 */
public class FocusPresenter implements Observer<Focus> {

    private static final String FOLLOWING = "Following: ";

    private final TextView view;

    public FocusPresenter(@NotNull TextView view) {
        this.view = view;
    }

    @Override
    public void update(@NotNull Observable<Focus> subject, @NotNull Focus state) {
        view.show(List.of(FOLLOWING + describe(state)));
    }

    private static @NotNull String describe(@NotNull Focus focus) {
        return switch (focus) {
            case Focus.Barycentre barycentre -> "barycentre";
            case Focus.OnBody(BodyName name) -> name.value();
        };
    }
}
