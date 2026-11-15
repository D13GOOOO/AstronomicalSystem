package astronomicalsystem.core.rings;

import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Rings of a planet: bands in the plane of its equator, around its centre.
 *
 * @param pole  direction of the north pole of the planet, perpendicular to the rings; precondition:
 *              a unit vector
 * @param bands the bands the rings are made of, which do not overlap
 */
public record Rings(@NotNull Vector3 pole, @NotNull List<RingBand> bands) {

    public Rings {
        bands = List.copyOf(bands);
    }
}
