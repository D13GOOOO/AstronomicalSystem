package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.physics.CollisionResolver;
import astronomicalsystem.core.physics.Integrator;
import astronomicalsystem.core.physics.TimeStep;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
     * @param bodies precondition: no two with the same name
     * @return the trajectory of every body, in the order of the bodies; a body that merges into
     *         another one stops where it was last seen, and the merged body goes on with the
     *         trajectory of the one whose name it keeps
     */
    public @NotNull List<Trajectory> predict(@NotNull List<Body> bodies) {
        Simulation future = new Simulation(integrator, collisions, bodies);
        // By name, since merges shift the bodies that follow them in the list; in the order of the
        // present bodies
        Map<BodyName, List<Vector3>> paths = new LinkedHashMap<>();
        addPositions(future.bodies(), paths);
        for (int point = 1; point < sampling.points(); point++) {
            advanceToTheNextPoint(future);
            addPositions(future.bodies(), paths);
        }
        return paths.entrySet().stream().map(path -> new Trajectory(path.getKey(), path.getValue())).toList();
    }

    private void advanceToTheNextPoint(@NotNull Simulation future) {
        for (int step = 0; step < sampling.stepsPerPoint(); step++) {
            future.advance(timeStep);
        }
    }

    private static void addPositions(@NotNull List<Body> bodies, @NotNull Map<BodyName, List<Vector3>> paths) {
        for (Body body : bodies) {
            paths.computeIfAbsent(body.name(), name -> new ArrayList<>()).add(body.position());
        }
    }
}
