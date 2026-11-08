package astronomicalsystem.app.presenter;

import astronomicalsystem.app.time.TimeControls;
import astronomicalsystem.app.view.KeyboardView;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TimeControlsPresenterTest {

    private final KeyboardView view = mock();
    private final TimeControls time = mock();
    private final TimeControlsPresenter SUT = new TimeControlsPresenter(view, time);

    @Test
    void thePresenterHandlesTheKeyboardOfItsView() {
        verify(view).addHandlers(SUT);
    }
}
