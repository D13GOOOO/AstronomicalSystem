package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.CameraModel;
import astronomicalsystem.app.camera.Viewport;
import astronomicalsystem.app.view.InputView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class CameraControlsPresenterTest {

    // 200 pixels of drag turn the camera by one radian. Dragging right turns the scene to the right,
    // so the camera goes the other way; dragging down lifts the camera above the scene
    private static final double TOLERANCE = 1e-12;
    private static final int NEW_WIDTH = 640;
    private static final int NEW_HEIGHT = 480;
    private static final double GRAB_X = 100.0;
    private static final double GRAB_Y = 100.0;
    private static final double TWENTY_PIXELS_RIGHT_AND_TEN_UP_X = 120.0;
    private static final double TWENTY_PIXELS_RIGHT_AND_TEN_UP_Y = 90.0;
    private static final double TWENTY_MORE_PIXELS_RIGHT_AND_TEN_MORE_UP_X = 140.0;
    private static final double TWENTY_MORE_PIXELS_RIGHT_AND_TEN_MORE_UP_Y = 80.0;
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

    // Each movement turns the camera by its own length, not by the distance from the grab
    @Test
    void everyMovementOfADragTurnsTheCameraByItsOwnLength() {
        SUT.pointerPressed(GRAB_X, GRAB_Y);
        SUT.pointerMoved(TWENTY_PIXELS_RIGHT_AND_TEN_UP_X, TWENTY_PIXELS_RIGHT_AND_TEN_UP_Y);

        SUT.pointerMoved(TWENTY_MORE_PIXELS_RIGHT_AND_TEN_MORE_UP_X, TWENTY_MORE_PIXELS_RIGHT_AND_TEN_MORE_UP_Y);

        verify(camera, times(2)).orbitBy(AZIMUTH_FOR_TWENTY_PIXELS_RIGHT, ELEVATION_FOR_TEN_PIXELS_UP);
    }

    // Every step of the wheel away from the user brings the camera 10% closer; towards the user it
    // undoes one such step; half steps come from touchpads
    @ParameterizedTest
    @CsvSource(textBlock = """
             1.0,  0.9
            -1.0,  1.1111111111111112
             2.0,  0.81
             0.5,  0.9486832980505138
            """)
    void theWheelMovesTheCameraCloserOrFarther(double steps, double factor) {
        SUT.scrolled(steps);

        ArgumentCaptor<Double> zoom = ArgumentCaptor.forClass(Double.class);
        verify(camera).zoomBy(zoom.capture());
        assertThat(zoom.getValue()).isCloseTo(factor, within(TOLERANCE));
    }

    @Test
    void theImageFollowsTheSizeOfTheWindow() {
        SUT.resized(NEW_WIDTH, NEW_HEIGHT);

        verify(camera).resize(new Viewport(NEW_WIDTH, NEW_HEIGHT));
    }

    // A minimised window has an empty image: the camera keeps its last size
    @ParameterizedTest
    @CsvSource(textBlock = """
              0,   0
              0, 480
            640,   0
            """)
    void anEmptyWindowLeavesTheCameraAlone(int width, int height) {
        SUT.resized(width, height);

        verifyNoInteractions(camera);
    }
}
