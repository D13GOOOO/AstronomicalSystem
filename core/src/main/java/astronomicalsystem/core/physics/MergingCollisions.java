package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Resolves every collision as a perfectly inelastic one: bodies that meet during the step merge
 * into one.
 */
public class MergingCollisions implements CollisionResolver {

    @Override
    public @NotNull List<Body> resolve(@NotNull List<Body> before, @NotNull List<Body> after) {
        List<Sweep> sweeps = new ArrayList<>(IntStream.range(0, after.size())
                .mapToObj(i -> new Sweep(before.get(i), after.get(i)))
                .toList());
        boolean merging;
        do {
            merging = mergeFirstMeeting(sweeps);
        } while (merging);
        return sweeps.stream().map(Sweep::end).toList();
    }

    /**
     * Replaces the first pair that meets, in index order, with the merged body, which takes the
     * place of the first of the two.
     *
     * @return whether a pair was merged
     */
    private static boolean mergeFirstMeeting(@NotNull List<Sweep> sweeps) {
        for (int i = 0; i < sweeps.size(); i++) {
            for (int j = i + 1; j < sweeps.size(); j++) {
                if (sweeps.get(i).meets(sweeps.get(j))) {
                    sweeps.set(i, sweeps.get(i).mergedWith(sweeps.remove(j)));
                    return true;
                }
            }
        }
        return false;
    }

    // A body over the step, moving in a straight line from where it starts to where it ends
    private record Sweep(@NotNull Body start, @NotNull Body end) {

        /**
         * @return whether the two spheres overlap at some moment of the step; their distance is
         *         smallest at the fraction of the step that minimises |d + w s|, where d is the
         *         separation at the start and w its change over the step
         */
        boolean meets(@NotNull Sweep other) {
            Vector3 startSeparation = other.start.position().minus(start.position());
            Vector3 change = other.end.position().minus(end.position()).minus(startSeparation);
            Vector3 closest = startSeparation.plus(change.times(closestFraction(startSeparation, change)));
            double reach = end.radius().astronomicalUnits() + other.end.radius().astronomicalUnits();
            return closest.lengthSquared() < reach * reach;
        }

        @NotNull Sweep mergedWith(@NotNull Sweep other) {
            return new Sweep(start.mergedWith(other.start), end.mergedWith(other.end));
        }

        // -d·w / |w|², kept within the step; at the start if the separation does not change
        private static double closestFraction(@NotNull Vector3 startSeparation, @NotNull Vector3 change) {
            double changeSquared = change.lengthSquared();
            if (changeSquared == 0.0) {
                return 0.0;
            }
            return Math.clamp(-startSeparation.dot(change) / changeSquared, 0.0, 1.0);
        }
    }
}
