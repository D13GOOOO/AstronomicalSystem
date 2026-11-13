package astronomicalsystem.app.presenter;

import astronomicalsystem.app.view.TextView;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ControlsHelpPresenterTest {

    private final TextView view = mock();

    @Test
    void listsTheControlsAsSoonAsItIsCreated() {
        new ControlsHelpPresenter(view);

        verify(view).show(List.of("Drag: turn  Wheel: zoom  +/-: faster/slower  Space: pause  ,/.: follow"));
    }
}
