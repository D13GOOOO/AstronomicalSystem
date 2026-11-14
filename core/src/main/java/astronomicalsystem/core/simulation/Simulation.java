package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.physics.Collision;
import astronomicalsystem.core.physics.CollisionResolver;
import astronomicalsystem.core.physics.Integrator;
import astronomicalsystem.core.physics.TimeStep;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

/**
 * State of a running simulation: the bodies of the system and how they evolve.
 * Collisions are resolved on creation, after every step and when a body joins, so no two bodies
 * ever overlap.
 */
public class Simulation implements Advanceable, Joinable {

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
        this.bodies = List.copyOf(collisions.resolve(initialBodies, initialBodies).bodies());
    }

    @Override
    public @NotNull List<Body> bodies() {
        return bodies;
    }

    /**
     * @return the collisions of the steps of the latest advance, in the order in which they happened
     */
    public @NotNull List<Collision> collisions() {
        return List.of();
    }

    public void advance(@NotNull TimeStep timeStep) {
        step(timeStep);
    }

    @Override
    public void add(@NotNull Body body) {
        if (bodies.stream().anyMatch(present -> present.name().equals(body.name()))) {
            throw new IllegalArgumentException("A body named " + body.name().value() + " is already in the simulation");
        }
        List<Body> joined = Stream.concat(bodies.stream(), Stream.of(body)).toList();
        bodies = List.copyOf(collisions.resolve(joined, joined).bodies());
    }

    /**
     * Takes several steps, one after the other.
     *
     * @param steps precondition: positive
     */
    @Override
    public void advance(@NotNull TimeStep timeStep, int steps) {
        for (int step = 0; step < steps; step++) {
            step(timeStep);
        }
    }

    // Not overridable, unlike the advances: a subclass that acts after an advance acts once however
    // many steps it takes
    private void step(@NotNull TimeStep timeStep) {
        bodies = List.copyOf(collisions.resolve(bodies, integrator.step(bodies, timeStep)).bodies());
    }
}
