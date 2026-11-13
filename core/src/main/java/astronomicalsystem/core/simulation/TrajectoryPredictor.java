package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.physics.CollisionResolver;
import astronomicalsystem.core.physics.Integrator;
import astronomicalsystem.core.physics.TimeStep;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class TrajectoryPredictor {

    public TrajectoryPredictor(@NotNull Integrator integrator, @NotNull CollisionResolver collisions,
                               @NotNull TimeStep timeStep, @NotNull Sampling sampling) {
    }

    public @NotNull List<Trajectory> predict(@NotNull List<Body> bodies) {
        return List.of();
    }
}
