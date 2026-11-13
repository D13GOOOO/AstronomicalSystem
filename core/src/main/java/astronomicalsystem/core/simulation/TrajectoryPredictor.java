package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.math.Vector3;
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
    private final Sampling sampling;

    public TrajectoryPredictor(@NotNull Integrator integrator, @NotNull CollisionResolver collisions,
                               @NotNull TimeStep timeStep, @NotNull Sampling sampling) {
        this.integrator = integrator;
        this.collisions = collisions;
        this.timeStep = timeStep;
        this.sampling = sampling;
    }

    /**
     * @return the trajectory of every body, in the order of the bodies
     */
    public @NotNull List<Trajectory> predict(@NotNull List<Body> bodies) {
        Simulation future = new Simulation(integrator, collisions, bodies);
        List<Body> present = future.bodies();
        List<List<Vector3>> paths = new ArrayList<>();
        for (Body body : present) {
            paths.add(new ArrayList<>(List.of(body.position())));
        }
        for (int point = 1; point < sampling.points(); point++) {
            advanceToTheNextPoint(future);
            addPositions(future.bodies(), paths);
        }
        List<Trajectory> trajectories = new ArrayList<>();
        for (int index = 0; index < present.size(); index++) {
            trajectories.add(new Trajectory(present.get(index).name(), paths.get(index)));
        }
        return trajectories;
    }

    private void advanceToTheNextPoint(@NotNull Simulation future) {
        for (int step = 0; step < sampling.stepsPerPoint(); step++) {
            future.advance(timeStep);
        }
    }

    // The bodies are aligned by index with the paths
    private static void addPositions(@NotNull List<Body> bodies, @NotNull List<List<Vector3>> paths) {
        for (int index = 0; index < bodies.size(); index++) {
            paths.get(index).add(bodies.get(index).position());
        }
    }
}
