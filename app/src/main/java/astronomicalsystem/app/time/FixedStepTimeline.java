package astronomicalsystem.app.time;

import astronomicalsystem.core.physics.TimeStep;
import astronomicalsystem.core.simulation.Advanceable;
import astronomicalsystem.core.time.JulianDate;
import org.jetbrains.annotations.NotNull;

/**
 * Lets simulated time flow with real time, sped up by a time warp that can be changed or paused,
 * in steps of the same length while the warp stays the same: a symplectic integrator keeps its
 * energy only with a constant step. The step is the longest one, unless the warp is so slow that a
 * real second would take fewer steps than it should: then it is shorter, so that time flows
 * smoothly.
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
    // Half a minute per second, slow enough to watch an impact
    private static final double SLOWEST_DAYS_PER_SECOND = 1.0 / 2880.0;

    private final Advanceable simulation;
    private final JulianDate start;
    private final TimeStep step;
    private final int leastStepsPerSecond;
    private TimeWarp warp;
    private boolean paused;
    // Simulated time already elapsed but shorter than a step, in days: it is spent in a later frame
    private double pendingDays;
    // The date is the start plus the simulated days elapsed: adding each step to a Julian date, some
    // two and a half million days, would lose the digits of the short steps
    private double elapsedDays;

    /**
     * @param start               date of the simulation when the timeline begins
     * @param step                the longest step
     * @param leastStepsPerSecond how many steps a real second takes at least, however slow the warp:
     *                            the step is shorter when the warp is slow; precondition: positive
     */
    public FixedStepTimeline(@NotNull Advanceable simulation, @NotNull JulianDate start, @NotNull TimeStep step,
                             @NotNull TimeWarp warp, int leastStepsPerSecond) {
        this.simulation = simulation;
        this.start = start;
        this.step = step;
        this.leastStepsPerSecond = leastStepsPerSecond;
        this.warp = warp;
    }

    @Override
    public void elapse(double realSeconds) {
        if (paused) {
            return;
        }
        pendingDays += Math.min(realSeconds, LONGEST_FRAME_SECONDS) * warp.daysPerSecond();
        TimeStep current = new TimeStep(Math.min(step.days(), warp.daysPerSecond() / leastStepsPerSecond));
        int steps = 0;
        while (pendingDays >= current.days()) {
            pendingDays -= current.days();
            steps++;
        }
        // All at once, so that the observers of the simulation hear from it once per frame; the date
        // is the end of the frame already, for those who ask while it advances
        if (steps > 0) {
            elapsedDays += steps * current.days();
            simulation.advance(current, steps);
        }
    }

    public @NotNull TimeFlow timeFlow() {
        JulianDate date = new JulianDate(start.days() + elapsedDays);
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
        if (canSlowDown()) {
            warp = new TimeWarp(warp.daysPerSecond() / SPEED_RATIO);
        }
    }

    private boolean canSlowDown() {
        return warp.daysPerSecond() / SPEED_RATIO >= SLOWEST_DAYS_PER_SECOND;
    }

    @Override
    public void slowest() {
        while (canSlowDown()) {
            warp = new TimeWarp(warp.daysPerSecond() / SPEED_RATIO);
        }
    }

    @Override
    public void togglePause() {
        paused = !paused;
    }
}
