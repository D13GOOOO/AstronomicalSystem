package astronomicalsystem.app.presenter;

import astronomicalsystem.app.follow.Focus;
import astronomicalsystem.app.view.TextView;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.observer.Observable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class FocusPresenterTest {

    private final TextView view = mock();
    private final Observable<Focus> follower = mock();
    private final FocusPresenter SUT = new FocusPresenter(view);

    static Stream<Arguments> focuses() {
        return Stream.of(
                Arguments.of(Focus.BARYCENTRE, "Following: barycentre"),
                Arguments.of(new Focus.OnBody(new BodyName("Earth")), "Following: Earth"));
    }

    @ParameterizedTest
    @MethodSource("focuses")
    void tellsWhatTheCameraFollows(Focus focus, String line) {
        SUT.update(follower, focus);

        verify(view).show(List.of(line));
    }
}
