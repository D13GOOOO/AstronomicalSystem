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

    private static final double PIXELS_PER_RADIAN = 200.0;

    private final CameraModel camera;
    private boolean dragging;
    // Where the pointer was grabbed
    private double grabX;
    private double grabY;

    public CameraControlsPresenter(@NotNull InputView view, @NotNull CameraModel camera) {
        this.camera = camera;
        view.addHandlers(this);
    }

    @Override
    public void pointerPressed(double x, double y) {
        dragging = true;
        grabX = x;
        grabY = y;
    }

    @Override
    public void pointerReleased() {
    }

    @Override
    public void pointerMoved(double x, double y) {
        if (!dragging) {
            return;
        }
        // Turning the scene the way the pointer goes takes the camera the opposite way around
        camera.orbitBy(-(x - grabX) / PIXELS_PER_RADIAN, (y - grabY) / PIXELS_PER_RADIAN);
    }

    @Override
    public void scrolled(double steps) {
    }

    @Override
    public void resized(int width, int height) {
    }
}
