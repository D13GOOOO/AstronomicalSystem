package astronomicalsystem.app.presenter;

import astronomicalsystem.app.time.TimeFlow;
import astronomicalsystem.app.time.TimeWarp;
import astronomicalsystem.app.view.TextView;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import astronomicalsystem.core.time.JulianDate;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Tells the user the date of the simulation and how fast simulated time flows.
 */
public class TimeFlowPresenter implements Observer<TimeFlow> {

    private static final DateTimeFormatter DATE_IN_UTC = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm 'UTC'",
            Locale.ROOT).withZone(ZoneOffset.UTC);
    // Up to two decimals, without trailing zeros
    private static final String SPEED_PATTERN = "0.##";
    private static final double HOURS_PER_DAY = 24.0;
    private static final String PAUSED = ", paused";

    private final TextView view;

    public TimeFlowPresenter(@NotNull TextView view) {
        this.view = view;
    }

    @Override
    public void update(@NotNull Observable<TimeFlow> subject, @NotNull TimeFlow state) {
        view.show(List.of(formatDate(state.date()), formatFlow(state)));
    }

    private static @NotNull String formatDate(@NotNull JulianDate date) {
        return DATE_IN_UTC.format(date.toInstant());
    }

    private static @NotNull String formatFlow(@NotNull TimeFlow flow) {
        String speed = formatSpeed(flow.warp());
        if (flow.paused()) {
            return speed + PAUSED;
        }
        return speed;
    }

    private static @NotNull String formatSpeed(@NotNull TimeWarp warp) {
        DecimalFormat speed = new DecimalFormat(SPEED_PATTERN, DecimalFormatSymbols.getInstance(Locale.ROOT));
        double daysPerSecond = warp.daysPerSecond();
        if (daysPerSecond >= 1.0) {
            return speed.format(daysPerSecond) + " d/s";
        }
        return speed.format(daysPerSecond * HOURS_PER_DAY) + " h/s";
    }
}
