package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.physics.Integrator;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * State of a running simulation: the bodies of the system and how they evolve.
 */
public class Simulation {

    public Simulation(@NotNull Integrator integrator, @NotNull List<Body> initialBodies) {
    }

    public @NotNull List<Body> bodies() {
        return List.of();
    }
}
