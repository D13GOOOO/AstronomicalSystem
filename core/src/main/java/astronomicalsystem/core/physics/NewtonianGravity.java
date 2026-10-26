package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Newtonian gravity with Plummer softening, computed over every pair of bodies.
 *
 * @param gravitationalConstant G, in AU cubed per solar mass per day squared
 * @param softeningLength       Plummer softening length, in AU
 */
public record NewtonianGravity(double gravitationalConstant, double softeningLength) implements GravityModel {

    @Override
    public @NotNull List<Vector3> accelerations(@NotNull List<Body> bodies) {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
