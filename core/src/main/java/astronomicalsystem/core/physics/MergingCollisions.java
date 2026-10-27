package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves every collision as a perfectly inelastic one: overlapping bodies merge into one.
 */
public class MergingCollisions implements CollisionResolver {

    @Override
    public @NotNull List<Body> resolve(@NotNull List<Body> bodies) {
        List<Body> resolved = new ArrayList<>(bodies);
        boolean merging;
        do {
            merging = mergeFirstOverlap(resolved);
        } while (merging);
        return resolved;
    }

    /**
     * Replaces the first overlapping pair, in index order, with the merged body, which takes the
     * place of the first of the two.
     *
     * @return whether a pair was merged
     */
    private static boolean mergeFirstOverlap(@NotNull List<Body> bodies) {
        for (int i = 0; i < bodies.size(); i++) {
            for (int j = i + 1; j < bodies.size(); j++) {
                if (bodies.get(i).overlaps(bodies.get(j))) {
                    bodies.set(i, bodies.get(i).mergedWith(bodies.remove(j)));
                    return true;
                }
            }
        }
        return false;
    }
}
