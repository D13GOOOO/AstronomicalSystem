package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.CameraModel;
import astronomicalsystem.app.view.InputView;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CameraControlsPresenterTest {

    private final InputView view = mock();
    private final CameraModel camera = mock();
    private final CameraControlsPresenter SUT = new CameraControlsPresenter(view, camera);

    @Test
    void thePresenterHandlesTheInputOfItsView() {
        verify(view).addHandlers(SUT);
    }
}
