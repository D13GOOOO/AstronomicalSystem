package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Computes the gravitational acceleration acting on every body of a system.
 */
public interface GravityModel {

    /**
     * @param bodies the bodies of the system
     * @return the accelerations, in AU per day squared; the element at index {@code i} is the
     *         acceleration of the body at index {@code i} of {@code bodies}
     */
    @NotNull List<Vector3> accelerations(@NotNull List<Body> bodies);
}
