package astronomicalsystem.app.time;

import astronomicalsystem.core.physics.TimeStep;
import astronomicalsystem.core.simulation.Advanceable;
import org.jetbrains.annotations.NotNull;

/**
 * Lets simulated time flow with real time, sped up by a time warp, always in steps of the same
 * length: a symplectic integrator keeps its energy only with a constant step.
 */
public class FixedStepTimeline {

    private final Advanceable simulation;
    private final TimeStep step;
    private final TimeWarp warp;

    public FixedStepTimeline(@NotNull Advanceable simulation, @NotNull TimeStep step, @NotNull TimeWarp warp) {
        this.simulation = simulation;
        this.step = step;
        this.warp = warp;
    }

    /**
     * @param realSeconds real time elapsed since the previous call; precondition: non-negative and
     *                    finite
     */
    public void elapse(double realSeconds) {
    }
}
