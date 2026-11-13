package astronomicalsystem.app.presenter;

import astronomicalsystem.app.follow.FollowControls;
import astronomicalsystem.app.time.TimeControls;
import astronomicalsystem.app.view.KeyboardView;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class KeyboardPresenterTest {

    private final KeyboardView view = mock();
    private final TimeControls time = mock();
    private final FollowControls follow = mock();
    private final KeyboardPresenter SUT = new KeyboardPresenter(view, time, follow);

    @Test
    void thePresenterHandlesTheKeyboardOfItsView() {
        verify(view).addHandlers(SUT);
    }

    // '=' is on the key of '+' where '+' needs the shift key
    static Stream<Arguments> commands() {
        return Stream.of(
                Arguments.of(character('+'), Named.of("speed up", (Consumer<TimeControls>) TimeControls::speedUp)),
                Arguments.of(character('='), Named.of("speed up", (Consumer<TimeControls>) TimeControls::speedUp)),
                Arguments.of(character('-'), Named.of("slow down", (Consumer<TimeControls>) TimeControls::slowDown)),
                Arguments.of(character(' '), Named.of("pause", (Consumer<TimeControls>) TimeControls::togglePause)));
    }

    @ParameterizedTest
    @MethodSource("commands")
    void everyCommandCharacterChangesTheFlowOfTime(int codePoint, Consumer<TimeControls> command) {
        SUT.characterTyped(codePoint);

        command.accept(verify(time));
    }

    // Keys without the shift key on both Italian and US layouts
    static Stream<Arguments> choicesOfFocus() {
        return Stream.of(
                Arguments.of(character('.'), Named.of("next", (Consumer<FollowControls>) FollowControls::followNext)),
                Arguments.of(character(','),
                        Named.of("previous", (Consumer<FollowControls>) FollowControls::followPrevious)));
    }

    @ParameterizedTest
    @MethodSource("choicesOfFocus")
    void thePunctuationChoosesWhatTheCameraFollows(int codePoint, Consumer<FollowControls> choice) {
        SUT.characterTyped(codePoint);

        choice.accept(verify(follow));
        verifyNoInteractions(time);
    }

    @ParameterizedTest
    // Also a letter outside ASCII: e with an acute accent
    @ValueSource(ints = {'a', '0', '_', 'é'})
    void anyOtherCharacterLeavesTimeAndCameraAlone(int codePoint) {
        SUT.characterTyped(codePoint);

        verifyNoInteractions(time, follow);
    }

    private static Named<Integer> character(char typed) {
        return Named.of("'" + typed + "'", (int) typed);
    }
}
