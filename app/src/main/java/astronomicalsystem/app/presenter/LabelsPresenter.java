package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.view.LabelsView;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class LabelsPresenter implements Observer<List<Body>> {

    public LabelsPresenter(@NotNull LabelsView view, @NotNull Projection projection,
                           @NotNull Map<BodyKind, BodyStyle> styles) {
    }

    @Override
    public void update(@NotNull Observable<List<Body>> subject, @NotNull List<Body> state) {
    }
}
