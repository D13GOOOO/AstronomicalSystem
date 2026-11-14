package astronomicalsystem.app.presenter;

import astronomicalsystem.app.view.TextView;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Tells the user how to move the camera, change the flow of time and launch impactors.
 */
public class ControlsHelpPresenter {

    // The controls of CameraControlsPresenter and KeyboardPresenter
    private static final List<String> CONTROLS = List.of(
            "Drag: turn  Wheel: zoom  +/-: faster/slower  Space: pause  ,/.: follow  1/2/3: impactor");

    public ControlsHelpPresenter(@NotNull TextView view) {
        view.show(CONTROLS);
    }
}
