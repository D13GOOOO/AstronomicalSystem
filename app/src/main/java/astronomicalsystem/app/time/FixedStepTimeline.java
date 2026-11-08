package astronomicalsystem.app.time;

import astronomicalsystem.core.physics.TimeStep;
import astronomicalsystem.core.simulation.Advanceable;
import org.jetbrains.annotations.NotNull;

/**
 * Lets simulated time flow with real time, sped up by a time warp, always in steps of the same
 * length: a symplectic integrator keeps its energy only with a constant step.
 * A frame longer than a second (the application was blocked) counts as one second: catching up
 * with the whole pause would take a burst of steps that blocks the application again.
 */
public class FixedStepTimeline implements Timeline, TimeControls {

    private static final double LONGEST_FRAME_SECONDS = 1.0;

    private final Advanceable simulation;
    private final TimeStep step;
    private final TimeWarp warp;
    // Simulated time already elapsed but shorter than a step, in days: it is spent in a later frame
    private double pendingDays;

    public FixedStepTimeline(@NotNull Advanceable simulation, @NotNull TimeStep step, @NotNull TimeWarp warp) {
        this.simulation = simulation;
        this.step = step;
        this.warp = warp;
    }

    @Override
    public void elapse(double realSeconds) {
        pendingDays += Math.min(realSeconds, LONGEST_FRAME_SECONDS) * warp.daysPerSecond();
        while (pendingDays >= step.days()) {
            simulation.advance(step);
            pendingDays -= step.days();
        }
    }

    @Override
    public void speedUp() {
    }
}
