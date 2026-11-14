package astronomicalsystem.core.rotation;

import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

/**
 * The axes fixed to a body, as unit vectors of space: the body turns, and they with it.
 *
 * @param x from the centre towards the point of the equator on the prime meridian
 * @param y from the centre towards the point of the equator 90 degrees east of it
 * @param z from the centre towards the north pole
 */
public record Axes(@NotNull Vector3 x, @NotNull Vector3 y, @NotNull Vector3 z) {
}
