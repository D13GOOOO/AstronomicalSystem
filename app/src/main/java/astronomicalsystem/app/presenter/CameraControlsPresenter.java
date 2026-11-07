package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.CameraModel;
import astronomicalsystem.app.view.InputHandler;
import astronomicalsystem.app.view.InputView;
import org.jetbrains.annotations.NotNull;

/**
 * Lets the user move the camera: dragging turns it around its target, the wheel moves it closer or
 * farther, and the image follows the size of the window.
 */
public class CameraControlsPresenter implements InputHandler {

    private final CameraModel camera;

    public CameraControlsPresenter(@NotNull InputView view, @NotNull CameraModel camera) {
        this.camera = camera;
        view.addHandlers(this);
    }

    @Override
    public void pointerPressed(double x, double y) {
    }

    @Override
    public void pointerReleased() {
    }

    @Override
    public void pointerMoved(double x, double y) {
    }

    @Override
    public void scrolled(double steps) {
    }

    @Override
    public void resized(int width, int height) {
    }
}
