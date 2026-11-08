package astronomicalsystem.app.time;

import astronomicalsystem.core.physics.TimeStep;
import astronomicalsystem.core.simulation.Advanceable;
import astronomicalsystem.core.time.JulianDate;
import org.jetbrains.annotations.NotNull;

/**
 * Lets simulated time flow with real time, sped up by a time warp that can be changed or paused,
 * always in steps of the same length: a symplectic integrator keeps its energy only with a constant
 * step.
 * A frame longer than a second (the application was blocked) counts as one second: catching up
 * with the whole pause would take a burst of steps that blocks the application again.
 */
public class FixedStepTimeline implements Timeline, TimeControls {

    private static final double LONGEST_FRAME_SECONDS = 1.0;
    // Every change doubles or halves the warp, and a change that would leave the limits is refused:
    // the warp stays a power of two times the first one, which it can always reach again exactly
    private static final double SPEED_RATIO = 2.0;
    // A year per second
    private static final double FASTEST_DAYS_PER_SECOND = 365.25;
    // An hour per second
    private static final double SLOWEST_DAYS_PER_SECOND = 1.0 / 24.0;

    private final Advanceable simulation;
    private final JulianDate start;
    private final TimeStep step;
    private TimeWarp warp;
    private boolean paused;
    // Simulated time already elapsed but shorter than a step, in days: it is spent in a later frame
    private double pendingDays;
    // The date is the start plus this many steps: adding each step to a Julian date would round it
    // every time, and the errors would pile up
    private long stepsTaken;

    /**
     * @param start date of the simulation when the timeline begins
     */
    public FixedStepTimeline(@NotNull Advanceable simulation, @NotNull JulianDate start, @NotNull TimeStep step,
                             @NotNull TimeWarp warp) {
        this.simulation = simulation;
        this.start = start;
        this.step = step;
        this.warp = warp;
    }

    @Override
    public void elapse(double realSeconds) {
        if (paused) {
            return;
        }
        pendingDays += Math.min(realSeconds, LONGEST_FRAME_SECONDS) * warp.daysPerSecond();
        while (pendingDays >= step.days()) {
            simulation.advance(step);
            pendingDays -= step.days();
            stepsTaken++;
        }
    }

    public @NotNull TimeFlow timeFlow() {
        JulianDate date = new JulianDate(start.days() + stepsTaken * step.days());
        return new TimeFlow(date, warp, paused);
    }

    @Override
    public void speedUp() {
        double faster = warp.daysPerSecond() * SPEED_RATIO;
        if (faster <= FASTEST_DAYS_PER_SECOND) {
            warp = new TimeWarp(faster);
        }
    }

    @Override
    public void slowDown() {
        double slower = warp.daysPerSecond() / SPEED_RATIO;
        if (slower >= SLOWEST_DAYS_PER_SECOND) {
            warp = new TimeWarp(slower);
        }
    }

    @Override
    public void togglePause() {
        paused = !paused;
    }
}
