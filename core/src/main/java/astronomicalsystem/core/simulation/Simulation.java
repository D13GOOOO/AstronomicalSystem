package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.physics.CollisionResolver;
import astronomicalsystem.core.physics.Integrator;
import astronomicalsystem.core.physics.TimeStep;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * State of a running simulation: the bodies of the system and how they evolve.
 * Collisions are resolved on creation and after every step, so no two bodies ever overlap.
 */
public class Simulation {

    private final Integrator integrator;
    private final CollisionResolver collisions;
    // A list rather than a map by name: the integrator and the gravity model work on lists aligned
    // by index, and no operation looks a body up by name yet. Always an unmodifiable copy, so that
    // it can be exposed without copying it again.
    private List<Body> bodies;

    public Simulation(@NotNull Integrator integrator, @NotNull CollisionResolver collisions,
                      @NotNull List<Body> initialBodies) {
        this.integrator = integrator;
        this.collisions = collisions;
        this.bodies = List.copyOf(collisions.resolve(initialBodies));
    }

    public @NotNull List<Body> bodies() {
        return bodies;
    }

    public void advance(@NotNull TimeStep timeStep) {
        bodies = List.copyOf(collisions.resolve(integrator.step(bodies, timeStep)));
    }
}
