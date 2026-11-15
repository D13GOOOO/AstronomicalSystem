package astronomicalsystem.app.sky;

import astronomicalsystem.core.math.Quantities;
import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

/**
 * Star of the background sky, so far that it is seen in the same direction from anywhere in the
 * system.
 *
 * @param direction unit vector towards the star
 * @param flux      light received from the star, in units of the light of the faintest star shown
 */
public record Star(@NotNull Vector3 direction, double flux) {

    /**
     * @param direction towards the star, of any length
     */
    public Star {
        if (direction.lengthSquared() == 0.0) {
            throw new IllegalArgumentException("A star needs a direction");
        }
        Quantities.requirePositiveFinite(flux, "Flux of a star");
        direction = direction.normalized();
    }
}
