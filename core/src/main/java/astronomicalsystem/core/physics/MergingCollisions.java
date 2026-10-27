package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Resolves every collision as a perfectly inelastic one: overlapping bodies merge into one.
 */
public class MergingCollisions implements CollisionResolver {

    @Override
    public @NotNull List<Body> resolve(@NotNull List<Body> bodies) {
        return bodies;
    }
}
