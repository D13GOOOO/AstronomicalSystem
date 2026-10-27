package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Resolves the collisions between the bodies of a system.
 */
public interface CollisionResolver {
    /**
     * @param bodies the bodies of the system
     * @return the bodies after every collision has been resolved, no two of which overlap
     */
    @NotNull List<Body> resolve(@NotNull List<Body> bodies);
}
