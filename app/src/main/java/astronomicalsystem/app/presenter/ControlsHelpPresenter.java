package astronomicalsystem.app.presenter;

import astronomicalsystem.app.view.TextView;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Tells the user how to move the camera and how to change the flow of time.
 */
public class ControlsHelpPresenter {

    // The controls of CameraControlsPresenter and TimeControlsPresenter
    private static final List<String> CONTROLS = List.of(
            "Drag: turn  Wheel: zoom  +/-: faster/slower  Space: pause");

    public ControlsHelpPresenter(@NotNull TextView view) {
        view.show(CONTROLS);
    }
}
