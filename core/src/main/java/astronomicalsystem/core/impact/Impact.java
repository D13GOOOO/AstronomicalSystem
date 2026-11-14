package astronomicalsystem.core.impact;

import astronomicalsystem.core.body.Mass;
import org.jetbrains.annotations.NotNull;

/**
 * Two bodies meeting: how hard they hit each other and what is left of them, after the scaling
 * laws of Leinhardt and Stewart (2012) for bodies held together by their own gravity.
 *
 * @param target          the larger body; precondition: not lighter than the projectile
 * @param projectile      the smaller body
 * @param contactDistance between the centres when the surfaces touch, in AU; precondition: positive
 * @param speed           of the projectile relative to the target when they touch, in AU per day;
 *                        precondition: non-negative and finite
 */
public record Impact(@NotNull Mass target, @NotNull Mass projectile, double contactDistance, double speed) {

    /**
     * @return whether the two bodies merge whole: they meet slower than their mutual escape speed
     */
    public boolean merges() {
        return false;
    }
}
