package astronomicalsystem.core.impact;

import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

/**
 * Where an impactor comes from, relative to the body it is going to hit.
 *
 * @param from  direction from the target towards where the impactor starts
 * @param aside direction, across the line of approach, towards which the impactor aims off the
 *              centre of the target
 */
public record Approach(@NotNull Vector3 from, @NotNull Vector3 aside) {

    public Approach {
        if (from.cross(aside).lengthSquared() == 0.0) {
            throw new IllegalArgumentException("Approach directions must not be parallel");
        }
    }
}
