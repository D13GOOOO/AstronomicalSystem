package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.CameraModel;
import astronomicalsystem.app.view.InputView;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class CameraControlsPresenterTest {

    // 200 pixels of drag turn the camera by one radian. Dragging right turns the scene to the right,
    // so the camera goes the other way; dragging down lifts the camera above the scene
    private static final double GRAB_X = 100.0;
    private static final double GRAB_Y = 100.0;
    private static final double TWENTY_PIXELS_RIGHT_AND_TEN_UP_X = 120.0;
    private static final double TWENTY_PIXELS_RIGHT_AND_TEN_UP_Y = 90.0;
    private static final double AZIMUTH_FOR_TWENTY_PIXELS_RIGHT = -0.1;
    private static final double ELEVATION_FOR_TEN_PIXELS_UP = -0.05;

    private final InputView view = mock();
    private final CameraModel camera = mock();
    private final CameraControlsPresenter SUT = new CameraControlsPresenter(view, camera);

    @Test
    void thePresenterHandlesTheInputOfItsView() {
        verify(view).addHandlers(SUT);
    }

    @Test
    void draggingTurnsTheCameraAroundItsTarget() {
        SUT.pointerPressed(GRAB_X, GRAB_Y);

        SUT.pointerMoved(TWENTY_PIXELS_RIGHT_AND_TEN_UP_X, TWENTY_PIXELS_RIGHT_AND_TEN_UP_Y);

        verify(camera).orbitBy(AZIMUTH_FOR_TWENTY_PIXELS_RIGHT, ELEVATION_FOR_TEN_PIXELS_UP);
    }

    @Test
    void movingThePointerWithoutPressingLeavesTheCameraAlone() {
        SUT.pointerMoved(TWENTY_PIXELS_RIGHT_AND_TEN_UP_X, TWENTY_PIXELS_RIGHT_AND_TEN_UP_Y);

        verifyNoInteractions(camera);
    }

    @Test
    void releasingTheButtonEndsTheDrag() {
        SUT.pointerPressed(GRAB_X, GRAB_Y);
        SUT.pointerReleased();

        SUT.pointerMoved(TWENTY_PIXELS_RIGHT_AND_TEN_UP_X, TWENTY_PIXELS_RIGHT_AND_TEN_UP_Y);

        verifyNoInteractions(camera);
    }
}
