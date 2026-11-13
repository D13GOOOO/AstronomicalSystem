package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.physics.CollisionResolver;
import astronomicalsystem.core.physics.Integrator;
import astronomicalsystem.core.physics.TimeStep;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Foresees the paths of the bodies by running a copy of their simulation ahead in time, with the
 * same integrator, collisions and time step: the prediction is what the simulation will do.
 */
public final class TrajectoryPredictor {

    private final Integrator integrator;
    private final CollisionResolver collisions;
    private final TimeStep timeStep;

    public TrajectoryPredictor(@NotNull Integrator integrator, @NotNull CollisionResolver collisions,
                               @NotNull TimeStep timeStep, @NotNull Sampling sampling) {
        this.integrator = integrator;
        this.collisions = collisions;
        this.timeStep = timeStep;
    }

    /**
     * @return the trajectory of every body, in the order of the bodies
     */
    public @NotNull List<Trajectory> predict(@NotNull List<Body> bodies) {
        Simulation future = new Simulation(integrator, collisions, bodies);
        List<Body> present = future.bodies();
        future.advance(timeStep);
        List<Trajectory> trajectories = new ArrayList<>();
        for (int index = 0; index < present.size(); index++) {
            trajectories.add(new Trajectory(present.get(index).name(),
                    List.of(present.get(index).position(), future.bodies().get(index).position())));
        }
        return trajectories;
    }
}
