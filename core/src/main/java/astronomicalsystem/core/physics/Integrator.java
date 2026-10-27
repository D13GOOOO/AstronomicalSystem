package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Advances the motion of a system of bodies in time.
 */
public interface Integrator {

    /**
     * @param bodies   the bodies of the system before the step
     * @param timeStep the duration of the step
     * @return the bodies after the step; the element at index {@code i} is the body at index
     *         {@code i} of {@code bodies}, with its new position and velocity
     */
    @NotNull List<Body> step(@NotNull List<Body> bodies, @NotNull TimeStep timeStep);
}
