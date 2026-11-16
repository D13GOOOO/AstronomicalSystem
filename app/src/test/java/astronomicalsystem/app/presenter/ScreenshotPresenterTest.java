package astronomicalsystem.app.presenter;

import astronomicalsystem.app.view.ScreenshotView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import java.nio.file.Path;
import java.time.Instant;
import java.time.InstantSource;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        ScreenshotPresenter SUT = new ScreenshotPresenter(view, PICTURES, InstantSource.fixed(now), zone);

        SUT.takeScreenshot();

        verify(view).capture(PICTURES.resolve(name));
    }

    // Keys typed during a slow frame arrive together: each picture still gets a file of its own,
    // and the names keep the order in which they were taken
    @Test
    void picturesTakenInTheSameMillisecondGoToSuccessiveFiles() {
        InstantSource time = mock();
        when(time.instant()).thenReturn(Instant.parse("2026-11-16T17:05:03.1234Z"),
                Instant.parse("2026-11-16T17:05:03.1237Z"), Instant.parse("2026-11-16T17:05:03.1242Z"));
        ScreenshotPresenter SUT = new ScreenshotPresenter(view, PICTURES, time, ZoneOffset.UTC);
        ArgumentCaptor<Path> files = ArgumentCaptor.forClass(Path.class);

        SUT.takeScreenshot();
        SUT.takeScreenshot();
        SUT.takeScreenshot();

        verify(view, times(3)).capture(files.capture());
        assertThat(files.getAllValues()).containsExactly(
                PICTURES.resolve("AstronomicalSystem-2026-11-16-17-05-03-123.png"),
                PICTURES.resolve("AstronomicalSystem-2026-11-16-17-05-03-124.png"),
                PICTURES.resolve("AstronomicalSystem-2026-11-16-17-05-03-125.png"));
    }
}
