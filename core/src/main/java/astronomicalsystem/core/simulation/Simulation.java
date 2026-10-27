package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.physics.Integrator;
import astronomicalsystem.core.physics.TimeStep;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * State of a running simulation: the bodies of the system and how they evolve.
 */
public class Simulation {

    private List<Body> bodies;

    public Simulation(@NotNull Integrator integrator, @NotNull List<Body> initialBodies) {
        this.bodies = initialBodies;
    }

    public @NotNull List<Body> bodies() {
        return bodies;
    }

    public void advance(@NotNull TimeStep timeStep) {
    }
}
