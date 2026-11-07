package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.CameraModel;
import astronomicalsystem.app.camera.Viewport;
import astronomicalsystem.app.view.InputHandler;
import astronomicalsystem.app.view.InputView;
import org.jetbrains.annotations.NotNull;

/**
 * Lets the user move the camera: dragging turns it around its target, the wheel moves it closer or
 * farther, and the image follows the size of the window.
 */
public class CameraControlsPresenter implements InputHandler {

    private static final double PIXELS_PER_RADIAN = 200.0;
    // Each step of the wheel scales the distance by the same ratio, so zooming feels the same at
    // every scale, from a planet to the whole solar system
    private static final double DISTANCE_RATIO_PER_STEP = 0.9;

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
        rememberPointerAt(x, y);
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
        rememberPointerAt(x, y);
    }

    @Override
    public void scrolled(double steps) {
        camera.zoomBy(Math.pow(DISTANCE_RATIO_PER_STEP, steps));
    }

    @Override
    public void resized(int width, int height) {
        // A minimised window has no image to fit
        if (width <= 0 || height <= 0) {
            return;
        }
        camera.resize(new Viewport(width, height));
    }

    private void rememberPointerAt(double x, double y) {
        previousX = x;
        previousY = y;
    }
}
