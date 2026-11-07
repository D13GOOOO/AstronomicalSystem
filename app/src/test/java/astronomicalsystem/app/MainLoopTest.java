package astronomicalsystem.app;

import astronomicalsystem.app.time.Clock;
import astronomicalsystem.app.time.Timeline;
import astronomicalsystem.app.view.Drawable;
import astronomicalsystem.app.view.Window;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MainLoopTest {

    private static final double START = 10.0;
    private static final double HALF_A_SECOND = 0.5;
    private static final double A_QUARTER_OF_A_SECOND = 0.25;

    private final Window window = mock();
    private final Clock clock = mock();
    private final Timeline timeline = mock();
    private final Drawable scene = mock();
    private final MainLoop SUT = new MainLoop(window, clock, timeline, scene);

    @Test
    void everyFrameElapsesTheRealTimeSinceThePreviousOne() {
        givenTwoFramesBeforeTheWindowCloses();

        SUT.run();

        InOrder inOrder = inOrder(timeline);
        inOrder.verify(timeline).elapse(HALF_A_SECOND);
        inOrder.verify(timeline).elapse(A_QUARTER_OF_A_SECOND);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void everyFrameIsDrawnAfterTheTimeHasElapsed() {
        givenTwoFramesBeforeTheWindowCloses();

        SUT.run();

        InOrder inOrder = inOrder(timeline, window);
        inOrder.verify(timeline).elapse(HALF_A_SECOND);
        inOrder.verify(window).drawFrame(scene);
        inOrder.verify(timeline).elapse(A_QUARTER_OF_A_SECOND);
        inOrder.verify(window).drawFrame(scene);
    }

    @Test
    void noFrameRunsInAWindowAlreadyClosed() {
        when(window.shouldClose()).thenReturn(true);

        SUT.run();

        verifyNoInteractions(timeline);
        verify(window, never()).drawFrame(any());
    }

    private void givenTwoFramesBeforeTheWindowCloses() {
        when(window.shouldClose()).thenReturn(false, false, true);
        when(clock.seconds()).thenReturn(START, START + HALF_A_SECOND,
                START + HALF_A_SECOND + A_QUARTER_OF_A_SECOND);
    }
}
