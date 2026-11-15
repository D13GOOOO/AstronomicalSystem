package astronomicalsystem.app.sky;

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
        direction = direction.normalized();
    }
}
