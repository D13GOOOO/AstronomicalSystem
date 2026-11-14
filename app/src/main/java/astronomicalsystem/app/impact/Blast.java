package astronomicalsystem.app.impact;

import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.physics.Collision;
import astronomicalsystem.core.time.JulianDate;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * What can be seen of an impact while it unfolds: a fireball on the site, and embers thrown up
 * that fall back onto the body.
 *
 * @param collision what happened
 * @param start     when it happened
 */
public record Blast(@NotNull Collision collision, @NotNull JulianDate start) {

    /**
     * @param survivor where the body that was hit is now
     * @return what glows at the date, in space
     */
    public @NotNull List<Flare> flaresAt(@NotNull JulianDate now, @NotNull Vector3 survivor) {
        return List.of();
    }
}
