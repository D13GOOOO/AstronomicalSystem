package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Resolves the collisions between the bodies of a system during a step of its motion.
 */
public interface CollisionResolver {
    /**
     * @param before the bodies at the start of the step
     * @param after  the bodies at the end of the step; precondition: the element at index {@code i}
     *               is the body at index {@code i} of {@code before}, moved (as an {@link Integrator}
     *               returns them), or the same body if the system has not moved
     * @return the bodies at the end of the step after every collision has been resolved, no two of
     *         which overlap
     */
    @NotNull List<Body> resolve(@NotNull List<Body> before, @NotNull List<Body> after);
}
