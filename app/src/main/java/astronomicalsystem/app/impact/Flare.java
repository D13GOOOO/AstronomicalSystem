package astronomicalsystem.app.impact;

import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

/**
 * Something glowing that an impact leaves for a while: its fireball or an ember of its plume.
 *
 * @param position in space, in AU
 * @param radius   in AU; 0 for an ember, too small to be seen at any scale
 * @param heat     from 1, as hot as it gets, to 0, cold
 */
public record Flare(@NotNull Vector3 position, double radius, double heat) {
}
