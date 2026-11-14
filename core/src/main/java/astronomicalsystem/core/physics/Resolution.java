package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * What a step leaves once its collisions are resolved.
 *
 * @param bodies     the bodies at the end of the step, no two of which overlap
 * @param collisions the collisions of the step, in the order in which they happened
 */
public record Resolution(@NotNull List<Body> bodies, @NotNull List<Collision> collisions) {

    public Resolution {
        bodies = List.copyOf(bodies);
        collisions = List.copyOf(collisions);
    }
}
