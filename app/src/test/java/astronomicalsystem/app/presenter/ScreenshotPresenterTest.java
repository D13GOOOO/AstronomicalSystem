package astronomicalsystem.app.presenter;

import astronomicalsystem.app.view.ScreenshotView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ScreenshotPresenterTest {

    private static final Path PICTURES = Path.of("pictures");

    private final ScreenshotView view = mock();

    // Rome is an hour ahead of UTC in November; a whole second still shows its milliseconds
    @ParameterizedTest
    @CsvSource(textBlock = """
            2026-11-16T17:05:03.123Z, UTC,         AstronomicalSystem-2026-11-16-17-05-03-123.png
            2026-11-16T17:05:03.123Z, Europe/Rome, AstronomicalSystem-2026-11-16-18-05-03-123.png
            2026-11-16T17:05:03Z,     UTC,         AstronomicalSystem-2026-11-16-17-05-03-000.png
            """)
    void aScreenshotGoesToAFileNamedAfterTheLocalTime(Instant now, ZoneId zone, String name) {
        ScreenshotPresenter SUT = new ScreenshotPresenter(view, PICTURES, Clock.fixed(now, zone));

        SUT.takeScreenshot();

        verify(view).capture(PICTURES.resolve(name));
    }
}
