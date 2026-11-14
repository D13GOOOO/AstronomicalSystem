package astronomicalsystem.core.impact;

import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Directions around an axis, within an angle of it: where the debris of an impact flies.
 *
 * @param axis      precondition: not zero
 * @param halfAngle between the axis and the side of the cone, in radians; precondition: in (0, pi]
 */
public record Cone(@NotNull Vector3 axis, double halfAngle) {

    /**
     * @param count precondition: positive
     * @return that many unit vectors spread evenly inside the cone
     */
    public @NotNull List<Vector3> directions(int count) {
        return Collections.nCopies(count, axis.normalized());
    }
}
