package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Velocity Verlet integrator (kick-drift-kick).
 */
public final class VelocityVerlet implements Integrator {

    private final GravityModel gravity;

    public VelocityVerlet(@NotNull GravityModel gravity) {
        this.gravity = gravity;
    }

    @Override
    public @NotNull List<Body> step(@NotNull List<Body> bodies, @NotNull TimeStep timeStep) {
        return List.of();
    }
}
