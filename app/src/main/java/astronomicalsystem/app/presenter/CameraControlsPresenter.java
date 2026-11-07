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
    // Where the pointer was at the previous event of the drag
    private double previousX;
    private double previousY;

    public CameraControlsPresenter(@NotNull InputView view, @NotNull CameraModel camera) {
        this.camera = camera;
        view.addHandlers(this);
    }

    @Override
    public void pointerPressed(double x, double y) {
        dragging = true;
        previousX = x;
        previousY = y;
    }

    @Override
    public void pointerReleased() {
        dragging = false;
    }

    @Override
    public void pointerMoved(double x, double y) {
        if (!dragging) {
            return;
        }
        // Turning the scene the way the pointer goes takes the camera the opposite way around
        camera.orbitBy(-(x - previousX) / PIXELS_PER_RADIAN, (y - previousY) / PIXELS_PER_RADIAN);
        previousX = x;
        previousY = y;
    }

    @Override
    public void scrolled(double steps) {
    }

    @Override
    public void resized(int width, int height) {
    }
}
