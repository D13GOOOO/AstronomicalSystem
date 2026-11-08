package astronomicalsystem.app.time;

import astronomicalsystem.core.time.JulianDate;
import org.jetbrains.annotations.NotNull;

/**
 * Where simulated time has got to and how fast it flows.
 *
 * @param date   of the simulation
 * @param warp   at which simulated time flows while it is not paused
 * @param paused whether simulated time is stopped
 */
public record TimeFlow(@NotNull JulianDate date, @NotNull TimeWarp warp, boolean paused) {
}
