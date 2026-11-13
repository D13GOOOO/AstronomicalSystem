package astronomicalsystem.app.presenter;

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

class TimeControlsPresenterTest {

    private final KeyboardView view = mock();
    private final TimeControls time = mock();
    private final TimeControlsPresenter SUT = new TimeControlsPresenter(view, time);

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

    @ParameterizedTest
    // Also a letter outside ASCII: e with an acute accent
    @ValueSource(ints = {'a', '0', '_', 'é'})
    void anyOtherCharacterLeavesTimeAlone(int codePoint) {
        SUT.characterTyped(codePoint);

        verifyNoInteractions(time);
    }

    private static Named<Integer> character(char typed) {
        return Named.of("'" + typed + "'", (int) typed);
    }
}
